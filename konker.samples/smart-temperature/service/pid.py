import time
from dao.pid import *
from controller.pid import *
from config import *

def control(key,input,Sp,min_output=None,max_output=None):
    dao = PidDao()

    def init_pid():
        if not dao.get_pid_entry_for(key):
            if not min_output or not max_output:
                raise Exception("Min and max output must be set!")
            dao.create_pid_entry(
                key=key,
                kp=PID_CONFIG['default_kp'],
                ki=PID_CONFIG['default_ki'],
                kd=PID_CONFIG['default_kd'],
                min_output=float(min_output),
                max_output=float(max_output))

        pid_id = dao.get_pid_entry_for(key)['id']
        Ci = Sp / PID_CONFIG['default_ki']
        dao.save_step(id=pid_id,err=0.0,Ci=Ci,sp=Sp,curr_time=0)

    def execute(step):
        if not step:
            init_pid()

        entry = dao.get_pid_entry_for(key)
        step = dao.get_last_step_for(key)
        current_sp = Sp or step['sp']

        controller = PID(kp=entry['kp'],ki=entry['ki'],kd=entry['kd'])
        out = controller.GenOut(
            input=input,Sp=current_sp,
            Ci=step['Ci'],last_error=step['err'],
            last_time=step['last_time']
        )

        out.update({'sp': current_sp})
        return out

    def save(new_step):
        def adjust_ci(entry):
            min_ci = entry['min_out'] / entry['ki']
            max_ci = entry['max_out'] / entry['ki']

            if new_step['Ci'] < min_ci:
                return min_ci
            elif new_step['Ci'] > max_ci:
                return max_ci
            else:
                return new_step['Ci']

        entry = dao.get_pid_entry_for(key)
        Ci = adjust_ci(entry)
        dao.save_step(
            id=entry['id'],
            err=new_step['error'],
            Ci=Ci,
            sp=new_step['sp'],
            curr_time=new_step['curr_time']
        )

    last_step = dao.get_last_step_for(key)
    if not last_step and not Sp:
        raise Exception("Initial Set point must be provided!")
    else:
        new_step = execute(last_step)
        save(new_step)
        return new_step['output']

def set(key,kp,ki,kd,min_output,max_output):
    dao = PidDao()
    dao.reset_pid_memory_for(key)
    dao.update_pid_entry(
        key,
        float(kp),
        float(ki),
        float(kd),
        float(min_output),
        float(max_output))

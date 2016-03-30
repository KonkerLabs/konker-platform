import time
from dao.pid import *
from controller.pid import *
from config import *

def control(key,input,Sp):
    dao = PidDao()

    def init_pid():
        if not dao.get_pid_entry_for(key):
            dao.create_pid_entry(key=key,kp=PID_CONFIG['default_kp'],ki=PID_CONFIG['default_ki'],kd=PID_CONFIG['default_kd'])

        pid_id = dao.get_pid_entry_for(key)['id']
        dao.save_step(id=pid_id,err=0.0,Ci=0.0,sp=Sp,curr_time=0)

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
        pid_id = dao.get_pid_entry_for(key)['id']
        dao.save_step(
            id=pid_id,
            err=new_step['error'],
            Ci=new_step['Ci'],
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

def set(key,kp,ki,kd):
    dao = PidDao()
    dao.reset_pid_memory_for(key)
    dao.update_pid_entry(key,kp,ki,kd)
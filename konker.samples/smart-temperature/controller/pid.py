import time

class PID:
    def __init__(self,kp,ki,kd):
        # initialze gains
        self.kp = kp
        self.ki = ki
        self.kd = kd

    def GenOut(self, input, Sp, Ci, last_error, last_time):

        curr_time = int(time.time())            # get t
        dt = 0 if last_time == 0 else 1         # get delta t
        
        error = Sp - input                      # get current error
        de = error - last_error                 # get delta error

        Cp = self.kp * error                    # proportional term
        Ci += error                             # integral term

        Cd = 0
        if dt > 0:                              # no div by zero
            Cd = de/dt                          # derivative term

        # sum the terms
        output = Cp + (self.ki * Ci) + (self.kd * Cd)

        # return the output along with its memory
        return {
            'curr_time': curr_time,
            'error': error,
            'Ci': Ci,
            'output': output
        }
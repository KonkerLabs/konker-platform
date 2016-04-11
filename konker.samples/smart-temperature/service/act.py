from config import *
from dao.act import *

LOWER_BOUND = ACT_CONFIG['range']['min']
UPPER_BOUND = ACT_CONFIG['range']['max']
ACT_TEMPLATE = "%sC"

def act(key,input):
    dao = ActDao()

    def get_last_command():
        return dao.get_last_command(key)
    def save_command(command):
        dao.save_command(key,command)
    def get_command(read):
        if read < LOWER_BOUND:
            return LOWER_BOUND
        elif read > UPPER_BOUND:
            return UPPER_BOUND
        else:
            return read

    read = int(round(input))
    last = get_last_command()

    command = get_command(read)
    save_command(command)

    if not last or last != command:
        return ACT_TEMPLATE % command
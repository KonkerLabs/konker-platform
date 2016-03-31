from config import *

LOWER_BOUND = ACT_CONFIG['range']['min']
UPPER_BOUND = ACT_CONFIG['range']['max']
ACT_TEMPLATE = "%sC"

def act(input):
    t = int(round(input))

    if t < LOWER_BOUND:
        return ACT_TEMPLATE % LOWER_BOUND
    elif t > UPPER_BOUND:
        return ACT_TEMPLATE % UPPER_BOUND
    else:
        return ACT_TEMPLATE % t
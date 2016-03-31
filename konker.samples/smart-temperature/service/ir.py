from dao.lookup_ir import *

def lookup_ir(brand,model,command):
    dao = LookupIRCodesDao()
    out = dao.get_ir_code_for(brand,model,command)
    if not out:
        raise Exception("Command not found for Brand / Model.")
    else:
        out['command'] = "IR"
        return out
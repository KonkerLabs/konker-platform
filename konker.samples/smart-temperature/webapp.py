from flask import *
import json
from service import pid as pid_service
from service import ir as ir_service
from service import act as act_service

app = Flask(__name__)

@app.route('/pid/control', methods=['POST'])
def pid_control():
    if not request.args.get('key',None):
        abort(400, "Key must be provided!")
    if not request.args.get('input',None):
        abort(400, "Input value must be provided!")
    if not request.headers.get('Content-Type') or request.headers.get('Content-Type') != "application/json":
        abort(400, "Invalid Content-Type")

    sp = request.args.get('Sp',None)
    result = pid_service.control(
        key=request.args['key'],
        input=float(request.args['input']),
        Sp=float(sp) if sp else None
    )
    
    response = request.json
    response['value'] = result
    return jsonify(response)

@app.route('/pid/set', methods=['POST'])
def pid_set_params():
    pid_service.set(key=request.args['key'],kp=request.args['kp'],ki=request.args['ki'],kd=request.args['kd'])
    return ""

@app.route('/lookup/<brand>/<model>/<command>/ircode', methods=['GET'])
def lookup_ir(brand,model,command):
    response = ir_service.lookup_ir(brand,model,command)
    return jsonify(response)

@app.route('/act', methods=['POST'])
def act():
    if not request.args.get('input',None):
        abort(400, "Input value must be provided!")
    if not request.headers.get('Content-Type') or request.headers.get('Content-Type') != "application/json":
        abort(400, "Invalid Content-Type")

    action = act_service.act(float(request.args['input']))

    response = request.json
    response['value'] = action
    return jsonify(response)

if __name__ == '__main__':
    app.run(debug=True)
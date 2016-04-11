from flask import *
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
    min_out = request.args.get('min_out',None)
    max_out = request.args.get('max_out',None)
    result = pid_service.control(
        key=request.args['key'],
        input=float(request.args['input']),
        Sp=float(sp) if sp else None,
        min_output=float(min_out) if min_out else None,
        max_output=float(max_out) if max_out else None
    )
    
    response = request.json
    response['value'] = result
    return jsonify(response)

@app.route('/pid/set', methods=['POST'])
def pid_set_params():
    if not request.args.get('key',None):
        abort(400, "Key must be provided!")

    pid_service.set(
        key=request.args['key'],
        kp=request.args['kp'],
        ki=request.args['ki'],
        kd=request.args['kd'],
        min_output=request.args['min_out'],
        max_output=request.args['max_out'])

    return ""

@app.route('/lookup/<brand>/<model>/<command>/ircode', methods=['GET'])
def get_lookup_ir(brand,model,command):
    response = ir_service.lookup_ir(brand,model,command)
    return jsonify(response)

@app.route('/lookup/ircode', methods=['POST'])
def post_lookup_ir():
    if not request.headers.get('Content-Type') or request.headers.get('Content-Type') != "application/json":
        abort(400, "Invalid Content-Type")

    brand = request.args['brand']
    model = request.args['model']
    command = request.args['command']

    if not brand:
        abort(400, "Brand must be provided!")
    if not model:
        abort(400, "Model must be provided!")
    if not command:
        abort(400, "Command must be provided!")

    response = ir_service.lookup_ir(brand,model,command)
    return jsonify(response)

@app.route('/act', methods=['POST'])
def act():
    if not request.args.get('key',None):
        abort(400, "Key must be provided!")
    if not request.args.get('input',None):
        abort(400, "Input value must be provided!")
    if not request.headers.get('Content-Type') or request.headers.get('Content-Type') != "application/json":
        abort(400, "Invalid Content-Type")

    action = act_service.act(key=request.args['key'],input=float(request.args['input']))

    if action:
        response = request.json
        fieldName = request.args.get('fieldName','value')
        response[fieldName] = action
        return jsonify(response)
    else:
        return ""

if __name__ == '__main__':
    app.run(debug=True)
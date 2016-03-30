from flask import *
import json
import service

app = Flask(__name__)

@app.route('/pid/control', methods=['POST'])
def control():
    if not request.args.get('key',None):
        abort(400, "Key must be provided!")
    if not request.args.get('input',None):
        abort(400, "Input value must be provided!")

    sp = request.args.get('Sp',None)
    result = service.control(
        key=request.args['key'],
        input=float(request.args['input']),
        Sp=float(sp) if sp else None
    )
    response = request.json
    response['value'] = result
    return jsonify(response)

@app.route('/pid/set', methods=['POST'])
def set_params():
    service.set(key=request.args['key'],kp=request.args['kp'],ki=request.args['ki'],kd=request.args['kd'])
    return ""

if __name__ == '__main__':
    app.run(debug=True)
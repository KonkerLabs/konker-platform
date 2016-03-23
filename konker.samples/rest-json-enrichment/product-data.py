#from flask import Flask
from flask import *
import json


def store(d):
    with open('data/product-data.json', 'w') as data_file:
        json.dump(d, data_file)
    
def load():
    with open('data/product-data.json') as data_file: 
        return json.load(data_file)


app = Flask(__name__)

@app.route("/device/<deviceid>/product", methods=["GET"])
def product(deviceid):
    if not deviceid in data:
        abort(404, "Device not found")
    return jsonify(data[deviceid])

@app.route("/device/<deviceid>/product", methods=["POST", "PUT"])
def saveProduct(deviceid):
    j = request.json
    if j:
        data[deviceid] = j
        store(data)
    else:
        abort(400, "Not valid JSON")
    return jsonify(data[deviceid])

@app.route("/device/<deviceid>/product", methods=["DELETE"])
def deleteProduct(deviceid):
    if not deviceid in data:
        abort(404, "Device not found")
    old = data[deviceid]
    del(data[deviceid])
    store(data)
    return jsonify(old)

@app.route("/load", methods=["POST", "PUT"])
def loadProducts():
    j = request.json
    if not j:
        abort(400, "Not valid JSON")
    else:
        data.clear()
        data.update(j)
        return jsonify({"STATUS": "OK"})


@app.route("/dump", methods=["GET"])
def dumpProducts():
    return jsonify(data)

data = load()

if __name__ == "__main__":
    app.run(debug=True)

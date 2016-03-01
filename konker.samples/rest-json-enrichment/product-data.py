#from flask import Flask
from flask import *
import json


with open('data/product-data.json') as data_file:    
    data = json.load(data_file)


app = Flask(__name__)

@app.route("/device/<deviceid>/product")
def product(deviceid):
    if not deviceid in data:
        abort(404, "Device not found")
    return jsonify(data[deviceid])

if __name__ == "__main__":
    app.run(debug=True)

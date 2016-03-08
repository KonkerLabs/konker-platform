from flask import *
from datetime import datetime
import random
import string
import collections
import json

app = Flask(__name__)
history = collections.deque([], 10)

@app.route("/transform/empty/response", methods=["POST"])
def empty_response():
    return ""

@app.route("/transform/empty/document", methods=["POST"])
def empty_document():
    return jsonify({})

@app.route("/transform/empty/list", methods=["POST"])
def empty_list():
    return "[]"

@app.route("/transform/remove/field/<field_name>", methods=["POST"])
def remove_field(field_name):
    d = request.json
    if field_name in d:
        del d[field_name]
    return jsonify(d)

@app.route("/transform/add/field/<field_name>/<value>/<field_type>", methods=["POST"])
@app.route("/transform/add/field/<field_name>/<value>", methods=["POST"])
def add_field(field_name, value, field_type="string"):
    d = request.json
    if field_type == "int":
        d[field_name] = int(value)
    else:
        d[field_name] = value
    return jsonify(d)

@app.route("/transform/randomize/field", methods=["POST"])
@app.route("/transform/randomize/field/<field_name>", methods=["POST"])
@app.route("/transform/randomize/field/<field_name>/<field_type>", methods=["POST"])
@app.route("/transform/randomize/field/<field_name>/<field_type>/<max_size>", methods=["POST"])
@app.route("/transform/randomize/field/<field_name>/<field_type>/<min_size>/<max_size>", methods=["POST"])
def randomize_field(field_name=None, field_type="string", max_size=8, min_size=1):
    d = request.json
    if not field_name:
        field_name = random_str()
    if field_type == "int":
        d[field_name] = random.randrange(int(min_size), int(max_size)+1)
    else:
        d[field_name] = random_str(int(min_size), int(max_size))
    return jsonify(d)

@app.route("/transform/randomize/document", methods=["GET", "POST"])
def randomize_document():
  return jsonify(random_json())

@app.route("/transform/abort", methods=["POST"])
def abort_request():
    abort(400, "Aborted")

@app.route("/transform/log", methods=["GET"])
def log_list():
    resp = Response(json.dumps([x for x in history], indent=4))
    resp.headers['Content-Type'] = 'application/json'
    return resp


def random_str(min_size=8, max_size=8):
    size = random.randrange(min_size, max_size+1)
    return ''.join(random.sample(string.ascii_lowercase, size))

def random_json(size=10):
   d = {} 
   if size <= 0:
       return d
   for i in range(random.randrange(1, size+1)):
       random_val = random.choice([lambda: random.randrange(size),
                                   lambda: random_str(1, size),
                                   lambda: random_json(size - 1)])
       d[random_str()] = random_val()
   return d


@app.after_request
def log(response):
    if (request.method == "POST"):
        history.appendleft({
                            "time": str(datetime.now()),
                            "req": {
                                   "url": request.url, 
                                   "body": request.data,
                             },
                            "resp": {
                                   "status": response.status,
                                   "body": response.data,
                             }})
    return response

if __name__ == "__main__":
    app.run(debug=True)

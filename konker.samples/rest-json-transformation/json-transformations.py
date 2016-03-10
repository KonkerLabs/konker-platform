from flask import *
from datetime import datetime
import random
import string
import collections
import json

app = Flask(__name__)
history = collections.deque([], 10)

docs = {
  "current": lambda request: current_json(request),
  "empty": lambda request: empty_json(request),
  "random": lambda request: random_json(request)
}

@app.route("/document/<doc>", methods=["GET", "POST"])
def document(doc):
    return jsonify(docs[doc](request))

@app.route("/document/<doc>/<field_name>", methods=["GET", "POST"])
def field(doc, field_name):
    d = docs[doc](request)
    o = {field_name: d[field_name]}
    return jsonify(o)

@app.route("/document/<doc>/<field_name>/remove", methods=["GET", "POST"])
def field_remove(doc, field_name):
    d = docs[doc](request)
    if field_name in d:
        del d[field_name]
    return jsonify(d)

@app.route("/document/<doc>/<field_name>/set", methods=["GET", "POST"])
def field_set(doc, field_name):
    d = docs[doc](request)
    if field_name in d:
        del d[field_name]
    value = None
    field_type = request.args.get('type', 'string') 
    if request.args.get('random_name', 'false') == 'true':
        field_name = random_str(8, 8)
    if request.args.get('value'):
        value = request.args.get('value')  
        if field_type == 'int':
            value = int(value)
        elif field_type == 'json':
            value = json.loads(value)
    if request.args.get('random_value', 'false') == 'true':
        if field_type == 'int':
            min_value = int(request.args.get('min', '0'))
            max_value = max(min_value, int(request.args.get('max', '10')))
            value = random_int(min_value, max_value)
        elif field_type == 'json':
            value = random_json()
        else:
            min_size = int(request.args.get('min', '1'))
            max_size = max(min_size, int(request.args.get('max', '10')))
            value = random_str(min_size, max_size)
    d[field_name] = value
    return jsonify(d)


@app.route("/literal/empty_list", methods=["GET", "POST"])
def empty_list():
    resp = Response("[]")
    resp.headers['Content-Type'] = 'application/json'
    return resp

@app.route("/literal/empty_text", methods=["GET", "POST"])
def empty_text():
    return ""

@app.route("/literal/abort", methods=["POST", "GET"])
def abort_request():
    abort(400, "Aborted")

@app.route("/admin/log", methods=["GET"])
def log_list():
    resp = Response(json.dumps([x for x in history], indent=4))
    resp.headers['Content-Type'] = 'application/json'
    return resp

@app.route("/admin/help", methods=["GET"])
def help():
    with open('README.txt') as f:
        resp = Response(f.read())
        resp.headers['Content-Type'] = 'text/plain'
        return resp

@app.after_request
def log(response):
    if ("/admin/" not in request.url):
        history.appendleft({
                            "time": str(datetime.now()),
                            "req": {
                                   "method": request.method,
                                   "url": request.url, 
                                   "body": request.data,
                             },
                            "resp": {
                                   "status": response.status,
                                   "body": response.data,
                             }})
    return response


def random_str(min_size=8, max_size=8):
    size = random.randrange(min_size, max_size+1)
    return ''.join(random.sample(string.ascii_lowercase, size))

def random_int(min_value=0, max_value=10):
    return random.randrange(min_value, (max_value+1))

def random_json(request=None, size=10):
   d = {} 
   if size <= 0:
       return d
   for i in range(random.randrange(1, size+1)):
       random_val = random.choice([lambda: random_int(0, size),
                                   lambda: random_str(1, size),
                                   lambda: random_json(size=(size - 1))])
       d[random_str()] = random_val()
   return d

def empty_json(request=None):
    return {}

def current_json(request):
    return json.loads(request.data)

if __name__ == "__main__":
    app.run(debug=True)

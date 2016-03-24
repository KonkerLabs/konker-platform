import requests
import xml.etree.ElementTree as ET

def request(action,response=lambda r: None):
    try:
        r = action()
        if r.status_code == requests.codes.ok:
            return r.text if response is not None else response(r)
    except Exception as e:
        print "Error on fetching response:", e
        return None

def get(url):
    return request(lambda: requests.get(url, auth=(APIKEY, '')))
def post(url,data):
    return request(lambda: requests.post(url, auth=(APIKEY, ''), data=data), lambda r: True)
def put(url,data):
    return request(lambda: requests.put(url, auth=(APIKEY, ''), data=data), lambda r: True)

def parse(text):
    if text is not None: return ET.fromstring(text)
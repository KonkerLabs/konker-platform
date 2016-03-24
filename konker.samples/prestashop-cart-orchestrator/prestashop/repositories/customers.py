import requests
import xml.etree.ElementTree as ET

GET_CUSTOMER_URL_TEMPLATE = "http://ec2-52-4-244-64.compute-1.amazonaws.com/konkershop/api/customers?filter[email]=%s"

def get_customer_code(email):
    def get_response(url):
        try:
            r = requests.get(url, auth=(APIKEY, ''))
            return r.text if r.status_code == requests.codes.ok else None
        except Exception as e:
            print "Error on response fetch:", e
            return None

    def parse(text):
        return ET.fromstring(text) if text is not None else text
    def customers(xml):
        return xml.find(".//customers") if xml is not None else xml

    customers = customers(parse(get_response(GET_CUSTOMER_URL_TEMPLATE % email)))
    return customers[0].attrib['id'] if customers is not None and len(customers) > 0 else None
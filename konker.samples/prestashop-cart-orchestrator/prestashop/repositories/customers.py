import xml.etree.ElementTree as ET
from .gateway import *

GET_CUSTOMER_URL_TEMPLATE = "http://ec2-52-4-244-64.compute-1.amazonaws.com/konkershop/api/customers?filter[email]=%s"

def get_customer_code(email):
    def customers(xml):
        if xml is not None: return xml.find(".//customers")

    customers = customers(parse(get(GET_CUSTOMER_URL_TEMPLATE % email)))
    if customers is not None and len(customers) > 0: return customers[0].attrib['id']
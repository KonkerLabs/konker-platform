import xml.etree.ElementTree as ET
from .gateway import *

GET_PRODUCT_URL_TEMPLATE = "http://ec2-52-4-244-64.compute-1.amazonaws.com/konkershop/api/products?filter[reference]=%s"

def get_product_code(reference):
    def products(xml):
        if xml is not None: return xml.find(".//products")

    products = products(parse(get(GET_PRODUCT_URL_TEMPLATE % reference)))
    if products is not None and len(products) > 0: return products[0].attrib['id']
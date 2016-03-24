import xml.etree.ElementTree as ET
from .gateway import *

CARTS_RESOURCE = "http://ec2-52-4-244-64.compute-1.amazonaws.com/konkershop/api/carts/"
FIND_LAST_CART_TEMPLATE = CARTS_RESOURCE + "?filter[id_customer]=%s&sort=[id_DESC]&limit=1"
GET_LAST_CART_TEMPLATE = CARTS_RESOURCE + "%s"

def add_to_cart(customer_code,product_code,quantity):
    def find_last():
        carts_xml = parse(get(FIND_LAST_CART_TEMPLATE % customer_code))
        if carts_xml is not None:
            carts = carts_xml.find(".//carts")
            if len(carts) > 0: return carts[0].attrib['id']

    def get_last_as_xml(cart_id):
        if cart_id is not None:
            return parse(get(GET_LAST_CART_TEMPLATE % cart_id))

    def new_cart():
        data = open("files/blank_cart.xml")
        xml = parse(data.read())
        xml.find(".//id_customer").text = str(customer_code)
        xml.find(".//id_product").text = str(product_code)
        xml.find(".//quantity").text = str(quantity)
        return xml

    def is_open(cart):
        return int(cart.find(".//id_carrier").text) == 0

    def is_product_missing(cart):
        return len(cart.findall(".//cart_row[id_product='%s']" % product_code)) == 0

    def append_to(cart):
        def new_element(name,value):
            el = ET.Element(name)
            el.text = str(value)
            return el
        
        new_row = ET.Element("cart_row")
        new_row.append(new_element("id_product",product_code))
        new_row.append(new_element("id_product_attribute",0))
        new_row.append(new_element("id_address_delivery",0))
        new_row.append(new_element("quantity",quantity))
        cart.find(".//cart_rows").append(new_row)

        return cart

    def save(cart,new=False):
        if new:
            return post(CARTS_RESOURCE, ET.tostring(cart))
        else:
            return put(CARTS_RESOURCE, ET.tostring(cart))

    last_cart_id = find_last()
    if last_cart_id is None:
        return save(new_cart(),new=True)
    else:
        last_cart = get_last_as_xml(last_cart_id)
        if is_open(last_cart):
            if is_product_missing(last_cart):
                return save(append_to(last_cart))
        else:
            return save(new_cart(),new=True)
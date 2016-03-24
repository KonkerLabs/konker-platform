import logging
from config import *
from prestashop.repositories import gateway
from prestashop.repositories import *

logger = logging.getLogger(__name__)
gateway.APIKEY = CONFIG['APIKEY']

def add_to_cart(email,reference,quantity):
    customer = customers.get_customer_code(email)
    product = products.get_product_code(reference)
    if customer and product:
        carts.add_to_cart(customer,product,quantity)
    else:
        logger.info("Customer or product not found!")
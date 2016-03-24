from prestashop.repositories import customers

customers.APIKEY = 'W79GDVSI32LLJA8BVVZGSYLUAX7FJCHB'

code = customers.get_customer_code("konker@konker.net")
print("Found: %s" % code)
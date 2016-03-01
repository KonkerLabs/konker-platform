Data enrichment sample application.

It reads data from data/product-data.json and servers slices of it on /device/<jsonKey>/product .

So, if data/product-data.json contains

{ "1": {
      "product": {"SKU": 123, "description": "20L Bonafont Water Bottle"},
      "quantity": 1,
      "storeUser": "johnsmith@nowhere.com"},
  "2": { 
       "product": {"SKU": 456, "description": "Nespresso Ristretto"},
       "quantity": 10,
       "storeUser": "mary@lamb.com"}
}


The expected returns is

GET /device/1/product
=====================
200: OK
{
  "product": {
    "SKU": 123, 
    "description": "20L Bonafont Water Bottle"
  }, 
  "quantity": 1, 
  "storeUser": "johnsmith@nowhere.com"
}


GET /device/2/product
=====================
200: OK
{
  "product": {
    "SKU": 456, 
    "description": "Nespresso Ristretto"
  }, 
  "quantity": 10, 
  "storeUser": "mary@lamb.com"
}


GET /device/3/product
=====================
404: Device not found

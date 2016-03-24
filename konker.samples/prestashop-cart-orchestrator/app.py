from flask import *
import logging
import json
import orchestrator

app = Flask(__name__)
logging.basicConfig(filename='orchestrator.log', level=logging.INFO)

@app.route('/cart/add', methods=['POST'])
def add_to_cart():
    data = request.json['product']
    orchestrator.add_to_cart(email=data['storeUser'],reference=data['productSKU'],quantity=data['quantity'])
    return "OK"

if __name__ == '__main__':
    app.run(debug=True)
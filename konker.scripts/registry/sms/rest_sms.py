#! /usr/bin/env python2
import os
import sqlite3
from twilio.rest import Client
from flask import Flask
from flask import request
from flask import g
from flask.templating import render_template

DATABASE = "~/rest_sms/db/database.db"

account_sid = "AC786215a11d359da4d322508ad9d77281"
auth_token = "845c385c42a4aa4808d02f19ef516246"
fromNumber = "+15132808127"
clientSMS = Client(account_sid, auth_token) 

app = Flask(__name__)

@app.route("/sms", methods=['POST'])
def sent() :
	interval = request.args['interval']
	to_ = str("+55") + request.args['to_']
	body = render_template('hello.txt', name=request.args['body'])
	
	message = clientSMS.api.messages.create(to=to_, from_=fromNumber, body=body)
	return render_template('hello.html')

def get_db():
	db = getattr(g, '_database', None)
	if db is None:
		db = g._database = sqlite3.connect(DATABASE)
	return db

@app.teardown_appcontext
def close_connection(exception):
	db = getattr(g, '_database', None)
	if db is not None:
		db.close()	

if __name__ == "__main__":
	app.run() 
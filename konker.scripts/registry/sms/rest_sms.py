#! /usr/bin/env python2
import os
import sqlite3
from twilio.rest import Client
from flask import Flask
from flask import request
from flask import g
from flask.templating import render_template
from datetime import datetime
from __builtin__ import Exception
from twisted.conch.scripts.conch import old
from jinja2.environment import Template
import json

DATABASE = "database.db"

account_sid = "AC786215a11d359da4d322508ad9d77281"
auth_token = "845c385c42a4aa4808d02f19ef516246"
fromNumber = "+15132808127"
clientSMS = Client(account_sid, auth_token) 

app = Flask(__name__)

@app.route("/sms", methods=['POST'])
def sent() :
	interval = request.args['interval']
	to_ = str("+55") + request.args['to_']
	
	template = Template(request.args['template'])
	message = template.render(request.json)

	validToSend = validate(interval, to_)
	if validToSend:
		message = clientSMS.api.messages.create(to=to_, from_=fromNumber, body=message)
		insert_db('insert into sms values(?, ?)', [to_, datetime.now()])
	
	return 'Sent'

def validate(interval, receiver): 
	sms = query_db('select * from sms where receiver = ? order by 2 desc', [receiver], one=True)
	
	if sms is None:
		return True
	else:
		now = datetime.now()
		old = datetime.strptime(sms[1], '%Y-%m-%d %H:%M:%S.%f')
		diff = now - old
		diffMin = diff.seconds/60
		
		if diffMin <= int(interval):
			return False
		else:
			return True	

def query_db(query, args=(), one=False):
	cursor = get_db().execute(query, args)
	rv = cursor.fetchall();
	cursor.close
	return (rv[0] if rv else None) if one else rv

def insert_db(sql, args=()):
	cursor = get_db().cursor()
	cursor.execute(sql, args)
	get_db().commit()

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
		
@app.before_first_request		
def create_table():
	create_table_sql = "CREATE TABLE IF NOT EXISTS sms (receiver text NOT NULL, sentDate date);"
	cursor = get_db().cursor()
	cursor.execute(create_table_sql)
	
if __name__ == "__main__":
	app.run()
	
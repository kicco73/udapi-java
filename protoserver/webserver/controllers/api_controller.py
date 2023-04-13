#!/usr/local/bin/python3

from flask import Response
from flask.helpers import request
from webserver import controller

from jobs import tbx2sparql

class ApiController(controller.Controller):

	def __init__(self, app, socketio):
		super().__init__(app, socketio)

		@app.route('/convert', methods = ['POST'])
		def convert():
			tbx = request.json.get('tbx')
			operation = tbx2sparql.Convert(tbx)
			sparql = operation.execute()
			return Response(sparql, mimetype='application/json')

#!/usr/local/bin/python3

from flask import Response
from flask.helpers import request
from webserver import controller
import json
import os

from jobs import analyse, convert, submit

class ApiController(controller.Controller):

	def __init__(self, app, socketio):
		super().__init__(app, socketio)

		@app.route('/resources', methods = ['POST'])
		def resources():
			tbx = request.json.get('tbx')
			operation = analyse.Analyse(tbx)
			resource_string = operation.execute()
			resource = json.loads(resource_string)
			self.save_resource(tbx, resource['id'], 'tbx')
			self.save_resource(resource['sparql'], resource['id'], 'sparql')
			self.save_resource(json.dumps(resource['metadata']), resource['id'], 'metadata')

			return Response(resource_string, mimetype='application/json')

		@app.route('/resources/<id>/submit', methods = ['POST'])
		def resources_id(id: str):
			repository = request.json.get('repository')
			infilename = os.path.join(self.app.config['RESOURCES_FOLDER'], id, 'sparql')
			operation = submit.Submit(infilename, repository)
			operation.execute()
			return Response(json.dumps({"repository": repository}), mimetype='application/json')

	def save_resource(self, content: str, resource_id: str, property: str):
		dirname = os.path.join(self.app.config['RESOURCES_FOLDER'], resource_id)
		filename = os.path.join(dirname, property)
		os.makedirs(dirname, exist_ok=True)
		with open(filename, mode='w') as file:
			file.write(content)

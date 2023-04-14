#!/usr/local/bin/python3

from flask import Response
from flask.helpers import request
from webserver import controller
import json
import os
import shutil

from jobs import analyse, convert, submit

class ApiController(controller.Controller):

	def __init__(self, app, socketio):
		super().__init__(app, socketio)

		@app.route('/resources', methods = ['POST'])
		def create_resource():
			tbx = request.json.get('tbx')
			operation = analyse.Analyse(tbx)
			resource_string = operation.execute()
			resource = json.loads(resource_string)

			del resource['sparql']	# FIXME: should not be produced

			self.delete_resource(resource['id'])
			self.save_resource_property(tbx, resource['id'], 'tbx')
			self.save_resource_property(json.dumps(resource['metadata']), resource['id'], 'metadata')

			return Response(json.dumps(resource), mimetype='application/json')

		@app.route('/resources/<resource_id>/sparql', methods = ['GET'])
		def get_resource_sparql(resource_id: str):
			content = self.read_resource_property(resource_id, 'sparql')
			if content is None:
				content = self.create_resource_sparql(resource_id)
			return Response(content, mimetype='application/sparql-query')

		@app.route('/resources/<resource_id>/submit', methods = ['POST'])
		def submit_resource_id(resource_id: str):
			filename = self.get_resource_path(resource_id, 'sparql')
			if not os.path.exists(filename):
				self.create_resource_sparql(resource_id)
			repository = request.json.get('repository')
			operation = submit.Submit(filename, repository)
			operation.execute()
			return Response(json.dumps({}), mimetype='application/json')

	def delete_resource(self, resource_id):
		dirname = self.get_resource_path(resource_id)
		shutil.rmtree(dirname, ignore_errors=True)

	def save_resource_property(self, content: str, resource_id: str, property: str):
		dirname = self.get_resource_path(resource_id)
		filename = os.path.join(dirname, property)
		os.makedirs(dirname, exist_ok=True)
		with open(filename, mode='w') as file:
			file.write(content)

	def read_resource_property(self, resource_id: str, property: str) -> str:
		filename = self.get_resource_path(resource_id, property)
		if os.path.exists(filename):
			with open(filename) as file:
				return file.read()
			
	def create_resource_sparql(self, resource_id):
		infilename = self.get_resource_path(resource_id, 'tbx')
		outfilename = self.get_resource_path(resource_id, 'sparql')
		operation = convert.Convert(infilename, outfilename)
		operation.execute()
		content = self.read_resource_property(resource_id, 'sparql')
		return content

	def get_resource_path(self, resource_id: str, *args) -> str:
		return os.path.join(self.app.config['RESOURCES_FOLDER'], resource_id, *args)

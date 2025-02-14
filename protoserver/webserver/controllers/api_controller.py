#!/usr/local/bin/python3

from flask import Response
from flask.helpers import request
from webserver import controller
import json
import os
import shutil

from jobs import services

class ApiController(controller.Controller):

	def __init__(self, app, socketio):
		super().__init__(app, socketio)

		@app.route('/resources', methods = ['POST'])
		def wrap_create_resource():
			tbx = request.json.get('tbx')
			operation = services.Analyse(input=tbx)
			operation.debug = app.config['DEBUG']
			resource_string = operation.execute()
			return Response(resource_string, mimetype='application/json')

		@app.route('/resources/<resource_id>', methods = ['GET'])
		def wrap_filter_resource(resource_id: str):
			languages = request.values.getlist('languages')
			dates = request.values.getlist('dates')
			subject_fields = request.values.getlist('subjectFields')
			operation = services.Filter(resource_dir=resource_id, languages=languages, dates=dates, subject_fields=subject_fields)
			operation.debug = app.config['DEBUG']
			content = operation.execute()
			return Response(content, mimetype='application/json')

		@app.route('/resources/<resource_id>/sparql', methods = ['GET'])
		def wrap_assemble_resource(resource_id: str):
			args = dict( 
				resource_dir = resource_id,
				languages = request.values.getlist('languages'),
				subject_fields = request.values.getlist('subjectFields'),
				dates = request.values.getlist('dates'),
				no_concepts = request.values.get('noConcepts', default=False, type=json.loads),
				no_senses = request.values.get('noSenses', default=False, type=json.loads),
				translate_terms = request.values.get('translateTerms', default=False, type=json.loads),
				translate_senses = request.values.get('translateSenses', default=False, type=json.loads),
				synonyms = request.values.get('synonyms', default=False, type=json.loads),
			)
			operation = services.Assemble(**args)
			operation.debug = app.config['DEBUG']
			content = operation.execute()
			return Response(content, mimetype='application/sparql-query')

		@app.route('/resources/<resource_id>/graphdb', methods = ['PUT'])
		def wrap_submit_resource(resource_id: str):
			inputdir = self.get_resource_path(resource_id)
			repository = request.json.get('repository')
			operation = services.Submit(resource_dir=inputdir, repository=repository)
			operation.debug = app.config['DEBUG']
			operation.execute()
			return Response(json.dumps({}), mimetype='application/json')

		@app.route('/resources/<resource_id>/graphdb', methods = ['GET'])
		def wrap_query_resource(resource_id: str):
			inputdir = self.get_resource_path(resource_id)
			repository = request.values.get('repository')
			operation = services.Query(resource_dir=inputdir, repository=repository)
			operation.debug = app.config['DEBUG']
			content = operation.execute()
			return Response(content, mimetype='application/json')

	def get_resource_path(self, resource_id: str, *args) -> str:
		return os.path.join(self.app.config['RESOURCES_FOLDER'], resource_id, *args)

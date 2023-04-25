#!/usr/bin/env python
# -*- coding: utf-8 -*-

from jobs.base import BaseOperation

class Analyse(BaseOperation):
	def execute(self) -> str:
		return self.run_java(
			'--service', 'analyse', 
			'--input-format', 'tbx',
		)

class Filter(BaseOperation):
	def __init__(self, resource_dir: str, languages: list, dates: list, subject_fields: list):
		super().__init__(resource_dir=resource_dir)
		self.languages = languages
		self.dates = dates
		self.subject_fields = subject_fields

	def execute(self) -> str:
		return self.run_java(
			'--service', 'filter', 
			'--filter-languages', ','.join(self.languages), 
			'--filter-dates', ','.join(self.dates), 
			'--filter-subjectfields', ','.join(self.subject_fields),
			'--', self.resource_dir,
		) 

class Assemble(BaseOperation):
	def __init__(self, resource_dir: str, languages: list, subject_fields: list, no_concepts: bool):
		super().__init__(resource_dir=resource_dir)
		self.languages = languages
		self.subject_fields = subject_fields
		self.no_concepts = no_concepts

	def execute(self) -> str:
		args = ['--filter-no-concepts'] if self.no_concepts else []
		args += [
			'--service', 'assemble', 
			'--filter-languages', ','.join(self.languages),
			'--filter-subjectfields', ','.join(self.subject_fields),
			'--', self.resource_dir,
		]
		return self.run_java(*args)

class Submit(BaseOperation):
	def __init__(self, resource_dir: str, repository: str):
		super().__init__(resource_dir=resource_dir)
		self.repository = repository

	def execute(self) -> str:
		return self.run_java(
			'--service', 'submit', 
			'--repository', self.repository, 
			'--', self.resource_dir,
		)

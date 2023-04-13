#!/usr/bin/env python
# -*- coding: utf-8 -*-

from jinja2 import BaseLoader, TemplateNotFound, Environment
from os.path import join, exists, getmtime

class TemplateLoader(BaseLoader):

    def __init__(self, path):
        super().__init__()
        self.path = path

    def get_source(self, environment, template):
        path = join(self.path, template)
        if not exists(path):
            raise TemplateNotFound(template)
        mtime = getmtime(path)
        with open(path, 'r') as f:
            source = f.read()
        return source, path, lambda: mtime == getmtime(path)

class HtmlRenderer(object):
    def __init__(self, pathname):
        loader = TemplateLoader('webserver/views')
        environment = Environment(loader=loader)
        self.template = environment.get_template(pathname)

    def render(self, **kw):
        html = self.template.render(**kw)
        return html

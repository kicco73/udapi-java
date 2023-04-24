#!/usr/local/bin/python3

import datetime
import logging
from flask.helpers import make_response
from flask.templating import render_template

class ComponentRenderer(object):

	@property
	def logger(self):
		return logging.getLogger(self.__class__.__name__)

	def __call__(self, template_name, **kw):
		text = render_template(template_name+'.jinja', **kw)
		return make_response(text)

	def __get_props(self, props):
		if not isinstance(props, dict):
			props = vars(props)
		return props

	def __is_equal(self, option_value, parm_value):
		if isinstance(parm_value, float):
			return float(option_value) == parm_value
		return str(option_value) == str(parm_value)

	def render_ul_component(self, items):
		output = '<ul>'
		for item in items:
			output += f'<li>{item}</li>'
		output += '</ul>'
		return output

	def render_select_component(self, parm_name, props, options, index=None, onchange_submit=False):
		props = self.__get_props(props)
		value = props.get(parm_name)
		if index is not None: value = value[index]
		onchange = 'onchange="this.form.submit()"' if onchange_submit else ''
		output = f'<select name="{parm_name}" {onchange}>'
		for option in options:
			option_value, option_text = option['value'], option['text']
			option_tuple_key = option_value[0] if isinstance(option_value, tuple) else option_value
			option_tuple_value = option_value[1] if isinstance(option_value, tuple) else option_value
			parm_value = value.get(option_tuple_key, '') if isinstance(value, dict) else value
			selected = "selected" if self.__is_equal(option_tuple_value, parm_value) else ""
			line = f'<option value="{str(option_value)}" {selected}>{option_text}</option>' 
			output += line
		output += '</select>'
		return output

	def render_input_color_component(self, parm_name, props):
		props = self.__get_props(props)
		if parm_name not in props: return ''
		return f'''<input type="color" name="{parm_name}" value="{props[parm_name]}">'''

	def input_checkbox_option_component(self, parm_name, props, option, onchange_submit):
		props = self.__get_props(props)
		onchange = 'onchange="this.form.submit()"' if onchange_submit else ''
		output = '<span><input type="checkbox" %(checked)s %(onchange)s name="%(parm_name)s" value="%(parm_value)s"><label>%(parm_text)s</label></span>' % {
			'parm_name': parm_name, 
			'parm_value': option['value'],
			'parm_text': f'<a href="{option["href"]}">{option["text"]}</a>' if 'href' in option else option['text'],
			'checked': 'checked' if option['value']in props[parm_name] else '',
			'onchange': onchange,
		}
		return output

	def render_input_hidden_component(self, parm_name, props):
		props = self.__get_props(props)
		if parm_name not in props: return ''
		value = props[parm_name]
		if isinstance(value, list):
			output = ''
			for v in value:
				output += f'<input type="hidden" name="{parm_name}" value="{v}">'
		elif isinstance(value, dict):
			output = ''
			for v in value.items():
				output += f'<input type="hidden" name="{parm_name}" value="{v}">'
		else:
			output = f'<input type="hidden" name="{parm_name}" value="{value}">'
		return output

	def render_input_checkbox_component(self, parm_name, props, options, onchange_submit=False):
		props = self.__get_props(props)
		if parm_name not in props: return ''
		output = '<div>'
		for option in options:
			output += self.input_checkbox_option_component(parm_name, props, option, onchange_submit)
		output += '</div>'
		return output

	def render_input_datetime_component(self, parm_name, props, min=None, max=None, onchange_submit=False):
		props = self.__get_props(props)
		if parm_name not in props: return ''
		dt : datetime.datetime = props[parm_name]
		value = dt.replace(tzinfo=None).isoformat()
		onchange = 'onchange="this.form.submit()"' if onchange_submit else ''
		return f'''
			<input type="datetime-local" name="{parm_name}" value="{value}" {onchange} min="{min}" max="{max}">
		'''

	def render_input_date_component(self, parm_name, props, min=None, max=None, onchange_submit=False):
		props = self.__get_props(props)
		if parm_name not in props: return ''
		dt : datetime.date = props[parm_name]
		value = dt.isoformat()
		onchange = 'onchange="this.form.submit()"' if onchange_submit else ''
		return f'''
			<input type="date" name="{parm_name}" value="{value}" {onchange} max="{max}" min="{min}">
		'''

	def render_text_component(self, parm_name, title, props, placeholder):
		props = self.__get_props(props)
		if parm_name not in props: return ''
		return '''
				<div>
					<label for="%(parm_name)s">%(title)s</label>
		   			<input type="text" name="%(parm_name)s" value="%(text_value)s" placeholder="%(placeholder)s">
					%(select_font_component)s
					%(select_height_component)s
					%(input_color_component)s
				</div>
			''' % {
				'title': title, 
				'placeholder': placeholder,
				'parm_name': parm_name, 
				'text_value': props[parm_name] or '',
				'input_color_component': self.render_input_color_component(parm_name + '_colour', props),
				'select_font_component': self.render_select_component(parm_name+'_font', props,
					[
						{'value': 'Brush Script', 'text': 'Brush Script'},
						{'value': 'Savoye LET', 'text': 'Savoye LET'},
						{'value': 'Phosphate', 'text': 'Phosphate'},
					]),
				'select_height_component': self.render_select_component(parm_name + '_height_mm', props,
					[{'value': value, 'text': str(value) + ' mm'} for value in range(5,21)])
			}

	def render_qrcode_component(self, parm_name, title, props):
		props = self.__get_props(props)
		if parm_name not in props: return ''
		return '''
				<div>
					<label for="%(parm_name)s">%(title)s</label>
					<input type="text" name="%(parm_name)s" value="%(parm_value)s" placeholder="Enter URL">
					<input type="color" name="%(parm_name)s_colour" value="%(colour_value)s">
				</div>
		''' % { 
			'title': title, 
			'parm_name': parm_name, 
			'parm_value': props[parm_name], 
			'colour_value': props[parm_name+'_colour']
		}
	
	def render_picture_component(self, parm_name, title, props):
		props = self.__get_props(props)
		if parm_name not in props: return ''
		return f'''
			<div>
				<label for="{parm_name}">{title}</label>
				<input type="file" name="{parm_name}" accept="image/png, image/jpeg">
			</div>
		'''

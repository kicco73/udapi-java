#!/usr/local/bin/python3

from flask import url_for

from webserver.views.component_renderer import ComponentRenderer
from datasources.entities.asset import Asset

class BeeRenderer(ComponentRenderer):

	def asset_icon(self, asset: Asset, line: str):
		#API_URL = f'https://cryptoicons.org/api/icon/{asset.id}/48'
		API_URL = f'https://cryptoicon-api.vercel.app/api/icon/{asset.id}'
		return f"""
		<div style="display: flex; flex-direction: row; width: 100%; align-items: center">
			<img src="{API_URL}" style="width: 36px; height: 36px; margin: 10px; filter: grayscale(75%);">
			<div>
				<div>{asset.name}</div>
				<div style="font-weight: normal; font-size: 12px">{line}</div>
			</div>
		</div>
		"""

	def exchange_icon(self, exchange_id: str, line: str):
		API_URL = url_for('assets_file', filename=f'{exchange_id}.png')
		return f"""
		<div style="display: flex; flex-direction: row; width: 100%; align-items: center">
			<img src="{API_URL}" style="width: 36px; height: 36px; margin: 10px; filter: grayscale(75%);">
			<div>
				<div>{exchange_id.capitalize()}</div>
				<div style="font-weight: normal; font-size: 12px">{line}</div>
			</div>
		</div>
		"""

	def big_trend_icon(self, up: bool, first_line: str, second_line: str):
		icon = 'trending_up' if up else 'trending_down'
		color = "green" if up else "red"
		return self.big_icon(icon=icon, color=color, first_line=first_line, second_line=second_line)

	def big_icon(self, icon: str, color: str, first_line: str, second_line: str):
		return f"""
		<div style="display: flex; flex-direction: row; width: 100%; align-items: center">
			<span class="material-icons md-36" style="color: {color}; width: 48px">{icon}</span>
			<div>
				<div>{first_line}</div>
				<div style="font-weight: normal; font-size: 12px">{second_line}</div>
			</div>
		</div>
		"""

	def warning_icon(self, message='', tip='Possible issue'):
		return f"""
		<span style="display: flex; align-items: center">
			<span title="{tip}" class="material-icons md-18" style="color: orange">warning</span>
			{message}
		</span>
		"""

	def clock_icon(self, message='', tip='Please hold on'):
		return f"""
		<span style="display: flex; align-items: center">
			<span title="{tip}" class="material-icons md-18" style="color: orange">watch_later</span>
			{message}
		</span>
		"""

	def trend_icon(self, prediction: str, message='', size=18):
		trend_icon = {'stay': 'trending_flat', 'buy': 'trending_up', 'sell': 'trending_down', None: 'error_outline'}
		return f"""
		<div style="display: flex; align-items: center">
			<span class="material-icons md-{size}" title="Current prediction is: {prediction}">{trend_icon[prediction]}</span>
			{message}
		</div>
		"""

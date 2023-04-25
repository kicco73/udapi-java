package cnr.ilc.rut.utils;

import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class JSONLogger {

	static private void sendMessage(String event, JSONObject message) {
		message.put("event", event);
		String output = JSONObject.toJSONString(message);
		System.err.println(output);
		System.err.flush();
	}

	static private void notify(String severity, String template, Object ...args) {
		JSONObject notification = new JSONObject();
		String detail = String.format(template, args);
		notification.put("severity", severity);
		notification.put("summary", "Information");
		notification.put("detail", detail);
		sendMessage("notification", notification);
	}

	static public void log(String template, Object ...args) {
		notify("info", template, args);
	}

	static public void warn(String template, Object ...args) {
		notify("warn", template, args);
	}

	static public void error(String template, Object ...args) {
		notify("error", template, args);
	}

	static public void progress(int percentage, String template, Object ...args) {
		JSONObject jobUpdate = new JSONObject();
		String job = String.format(template, args);
		jobUpdate.put("job", job);
		jobUpdate.put("progress", percentage);
		sendMessage("job update", jobUpdate);
	}
}

package cnr.ilc.rut;

public class Logger {

	static public void log(String template, Object ...args) {
		JSONLogger.log(template, args);
	}

	static public void warn(String template, Object ...args) {
		JSONLogger.warn(template, args);
	}

	static public void error(String template, Object ...args) {
		JSONLogger.log(template, args);
	}

	static public void progress(int percentage, String template, Object ...args) {
		JSONLogger.progress(percentage, template, args);
	}
}

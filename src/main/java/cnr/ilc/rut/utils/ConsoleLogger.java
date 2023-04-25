package cnr.ilc.rut.utils;

public class ConsoleLogger {
	private static final String ANSI_RESET = "\u001B[0m";
	private static final String ANSI_GREEN = "\u001B[32m";
	private static final String ANSI_YELLOW = "\u001B[33m";
	private static final String ANSI_RED = "\u001B[31m";

	static private void println(String colour, String template, Object ...args) {
		String output = String.format(colour + template + ANSI_RESET, args);
		System.err.println(output);
	}

	static public void log(String template, Object ...args) {
		println(ANSI_GREEN, template, args);
	}

	static public void warn(String template, Object ...args) {
		println(ANSI_YELLOW, template, args);
	}

	static public void error(String template, Object ...args) {
		println(ANSI_RED, template, args);
	}
}

package cnr.ilc.sparql;

import java.util.regex.Pattern;

public class TripleOptimiser {
	static private Pattern removeBlanks = Pattern.compile("[\n\t ]+");
	StringBuilder builder = new StringBuilder("");
	private String lastSubject = "";
	private boolean unterminated = false;

	private void endLineIfUnterminated() {
		if (!unterminated) return;
		builder.append(" .\n");
		unterminated = false;
		lastSubject = "";
	}

	public void append(String content) {
		endLineIfUnterminated();
		builder.append(content);
	}

	public void add(String subject, String link, String object) {
		String content;
		object = removeBlanks.matcher(object).replaceAll(" ");

		if (subject.equals(lastSubject)) {
			content = String.format(" ; %s %s", link, object);
		} else {
			endLineIfUnterminated();
			content = String.format("\t%s %s %s", subject, link, object);
			lastSubject = subject;
		}

		builder.append(content);
		unterminated = true;
	} 
	
	public StringBuilder getStringBuilder() {
		endLineIfUnterminated();
		return builder;
	}

}

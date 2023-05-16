/**
 * @author Enrico Carniani
 */

package cnr.ilc.sparql;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

public class SPARQLFormatter {
	static private Pattern removeBlanks = Pattern.compile("[\n\t ]+");

	static public String formatObjectWithUrlIfPossible(String object) {
		try {
			new URL(object);
			object = String.format("<%s>", object);
		}
		catch (MalformedURLException e) {
			object = formatObjectAsString(object);
		}
		return object;
	}

	static public String formatObjectAsStringWithLanguage(String object, String language) {
		object = String.format("%s@%s", formatObjectAsString(object), language);
		return object;
	}

	static public String formatObjectAsString(String object) {
		object = object.replaceAll("\"", "\\\\\"");
		object = object.replaceAll("\n", "\\\\n");
		object = String.format("\"%s\"", object.trim());
		return object;
	}

	static private String formatMultipleObjects(String... links) {
		StringBuilder object = new StringBuilder("");

		int count = 0;
		
		while(count < links.length) {
			object.append(links[count++] + " " + links[count++]);
			if (count < links.length) object.append(" ; ");
		}
		String renderedObject = removeBlanks.matcher(object.toString()).replaceAll(" ");
		return renderedObject;
	}

	static public String formatStatement(String subject, String link, String object) {
		return String.format("\t%s %s %s .\n", subject, link, object);
	}

	static public String formatMultipleStatement(String subject, String... links) {
		String object = formatMultipleObjects(links);
		return String.format("\t%s %s .\n", subject, object);
	}

	static public String formatAnonStatement(String subject, String link, String... anon) {
		String object = formatMultipleObjects(anon);
		return String.format("\t%s %s [ %s ] .\n", subject, link, object);
	}
}

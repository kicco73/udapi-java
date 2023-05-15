/**
 * @author Enrico Carniani
 */

package cnr.ilc.sparql;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

public class SPARQLFormatter {

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

	static public String formatObjectWithLanguage(String object, String language) {
		object = String.format("%s@%s", formatObjectAsString(object), language);
		return object;
	}

	static public String formatObjectAsString(String object) {
		object = object.replaceAll("\"", "\\\\\"");
		object = object.replaceAll("\n", "\\\\n");
		object = String.format("\"%s\"", object.trim());
		return object;
	}

	static private String flattenObject(Map<String, String> links) {
		String object = "";
		int count = links.size();
		SortedSet<String> keys = new TreeSet<>(links.keySet());
		for (String key: keys) {
			object += key + " " + links.get(key);
			if (--count > 0) object += " ; ";
		}
		object = object.replaceAll("[\n\t ]+", " ");
		return object;
	}

	static public String formatStatement(String subject, String link, String object) {
		return String.format("\t%s %s %s .\n", subject, link, object);
	}

	static public String formatMultipleStatement(String subject, Map<String,String> links) {
		String object = flattenObject(links);
		return String.format("\t%s { %s } .\n", subject, object);
	}

	static public String formatAnonStatement(String subject, String link, Map<String,String> anon) {
		String object = flattenObject(anon);
		return String.format("\t%s %s [ %s ] .\n", subject, link, object);
	}
}

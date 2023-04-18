/**
 * @author Enrico Carniani
 */

package cnr.ilc.rut;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
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

	static public String formatObject(Map<String, String> anon) {
		String object = "[ ";
		int count = anon.size();
		for (Entry<String,String> entry: anon.entrySet()) {
			object += entry.getKey() + " " + entry.getValue();
			if (--count > 0) object += " ; ";
		}
		object += " ]";
		return object;
	}
}

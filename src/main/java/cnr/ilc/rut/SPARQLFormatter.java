/**
 * @author Enrico Carniani
 */

package cnr.ilc.rut;

import java.net.MalformedURLException;
import java.net.URL;

public class SPARQLFormatter {

	static public String formatObjectWithUrlIfPossible(String object) {
		try {
			new URL(object);
			object = String.format("<%s>", object);
		}
		catch (MalformedURLException e) {
			object = formatObject(object);
		}
		return object;
	}

	static public String formatObjectWithLanguage(String object, String language) {
		object = String.format("%s@%s", formatObject(object), language);
		return object;
	}

	static public String formatObject(String object) {
		object = object.replaceAll("\"", "\\\\\"");
		object = object.replaceAll("\n", "\\\\n");
		object = String.format("\"%s\"", object.trim());
		return object;
	}
}

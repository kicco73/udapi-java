/**
 * @author Enrico Carniani
 */

package cnr.ilc.conllu.main;

import java.util.HashMap;
import java.util.Map;

public class Form {
	final String FQName;
	final String text;
	final Map<String, String> features;

	public Form(String FQName, String text) {
		this.FQName = FQName.replaceAll("[\\.']", "-");;
		this.text = text;
		features = new HashMap<>();
	}
}

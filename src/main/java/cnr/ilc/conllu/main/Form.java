/**
 * @author Enrico Carniani
 */

package cnr.ilc.conllu.main;

import java.util.HashMap;
import java.util.Map;

public class Form {
	final String text;
	final Map<String, String> features;

	public Form(String text) {
		this.text = text;
		features = new HashMap<>();
	}
}

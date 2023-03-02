/**
 * @author Enrico Carniani
 */

package cz.ufal.udapi.main;

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

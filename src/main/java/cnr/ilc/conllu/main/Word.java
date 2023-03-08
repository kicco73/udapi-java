/**
 * @author Enrico Carniani
 */

package cnr.ilc.conllu.main;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Word {
	final String FQName;
	final Form canonicalForm;
	final String partOfSpeech;
	final private Map<String, Form> otherForms;

	public Word(String lemma, String partOfSpeech) {
		
		lemma = lemma.toLowerCase();
		String FQName = lemma.replaceAll("[\\.']", "-");

		if (lemma != FQName) {
			System.err.println(String.format("Warning: found lemma %s, using FQName %s", lemma, FQName));
		}

		this.FQName = String.format(":le_%s", FQName);
		canonicalForm = new Form(lemma);
		this.partOfSpeech = partOfSpeech;
		otherForms = new HashMap<>();
	}

	public void addOtherForm(Form form) {
		otherForms.put(form.text, form);
	}

	public Collection<Form> getOtheForms() {
		return otherForms.values();
	}

	public boolean hasForm(String text) {
		return canonicalForm.text.equals(text) || otherForms.keySet().contains(text);
	}
}

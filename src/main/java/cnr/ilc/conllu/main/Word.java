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
	final String language;
	final private Map<String, Form> otherForms;

	public Word(String lemma, String partOfSpeech, String language) {
		
		lemma = lemma.toLowerCase();
		String FQName = lemma.replaceAll("[\\.' ]", "-");

		if (lemma != FQName) {
			System.err.println(String.format("Warning: found lemma %s, using FQName %s", lemma, FQName));
		}

		this.FQName = String.format(":le_%s_%s", FQName, partOfSpeech.split(":")[1]);
		String canonicalFormFQN = String.format("%s_lemma", this.FQName);
		canonicalForm = new Form(canonicalFormFQN, lemma);
		this.partOfSpeech = partOfSpeech;
		this.language = language;
		otherForms = new HashMap<>();
	}

	public void addOtherForm(Form form) {
		otherForms.put(form.text, form);
	}

	public Collection<Form> getOtheForms() {
		return otherForms.values();
	}

	public Form findForm(String text) {
		if (canonicalForm.text.equals(text)) return canonicalForm;
		return otherForms.get(text);
	}

}

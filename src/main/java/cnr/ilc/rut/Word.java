/**
 * @author Enrico Carniani
 */

package cnr.ilc.rut;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Word {
	public final String FQName;
	public final Form canonicalForm;
	public final String partOfSpeech;
	public final String language;
	public String conceptFQN;
	final private Map<String, Form> otherForms = new HashMap<>();

	public Word(String lemma, String partOfSpeech, String language) {
		
		lemma = lemma.toLowerCase();
		String FQName = lemma.replaceAll("[\\.\\(\\)/' ]", "-");

		if (lemma != FQName) {
			System.err.println(String.format("Warning: found lemma %s, using FQName %s", lemma, FQName));
		}

		if (partOfSpeech != null) {
			this.FQName = String.format(":le_%s_%s", FQName, partOfSpeech.split(":")[1]);
		} else {
			this.FQName = String.format(":le_%s", FQName);
		}

		String canonicalFormFQN = String.format("%s_lemma", this.FQName);
		canonicalForm = new Form(canonicalFormFQN, lemma);
		this.partOfSpeech = partOfSpeech;
		this.language = language;
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

/**
 * @author Enrico Carniani
 */

package cnr.ilc.rut;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Word extends SPARQLEntity {
	final public String FQName;
	final public Form canonicalForm;
	final public String partOfSpeech;
	final public String language;
	final public WeakReference<Concept> concept;
	final public String lexiconFQN;
	final private String rdfType;
	final public Map<String, String> senses = new HashMap<>();
	final private Map<String, Form> otherForms = new HashMap<>();
	static final private IdGenerator idGenerator = new IdGenerator();

	public Word(String lemma, String partOfSpeech, String language, Concept concept, String lexiconFQN, String rdfType) {
		this.partOfSpeech = partOfSpeech;
		this.language = language;
		this.lexiconFQN = lexiconFQN;
		this.rdfType = rdfType;
		this.concept = concept == null? null : new WeakReference<Concept>(concept);

		String FQName = idGenerator.getId(String.format("%s+%s+%s", lemma, partOfSpeech, language));

		if (partOfSpeech != null) {
			this.FQName = String.format(":le_%s_%s", FQName, partOfSpeech.split(":")[1]);
		} else {
			this.FQName = String.format(":le_%s", FQName);
		}

		String canonicalFormFQN = String.format("%s_lemma", this.FQName);
		canonicalForm = new Form(canonicalFormFQN, lemma);

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

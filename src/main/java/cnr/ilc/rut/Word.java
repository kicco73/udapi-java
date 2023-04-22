/**
 * @author Enrico Carniani
 */

package cnr.ilc.rut;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import cnr.ilc.sparql.TripleSerialiser;
import cnr.ilc.sparql.WordSerialiser;

public class Word {
	final public String FQName;
	final public Form canonicalForm;
	final public String partOfSpeech;
	final public String language;
	final public WeakReference<Concept> concept;
	final public String lexiconFQN;
	final public String rdfType;
	final public String creator;
	final public TripleSerialiser triples;
	final public Metadata metadata = new Metadata();
	final public Map<String, String> senses = new LinkedHashMap<>();
	final private Map<String, Form> otherForms = new LinkedHashMap<>();
	static final private IdGenerator idGenerator = new IdGenerator();

	public Word(String lemma, String partOfSpeech, String language, Concept concept, String lexiconFQN, String rdfType, String creator) {
		this.partOfSpeech = partOfSpeech;
		this.language = language;
		this.lexiconFQN = lexiconFQN;
		this.rdfType = rdfType;
		this.creator = creator;
		this.concept = concept == null? null : new WeakReference<Concept>(concept);

		String FQName = idGenerator.getId(String.format("%s+%s+%s", lemma, partOfSpeech, language));

		if (partOfSpeech != null) {
			this.FQName = String.format(":le_%s_%s", FQName, partOfSpeech.split(":")[1]);
		} else {
			this.FQName = String.format(":le_%s", FQName);
		}

		String canonicalFormFQN = String.format("%s_lemma", this.FQName);
		canonicalForm = new Form(canonicalFormFQN, lemma);

		this.triples = new WordSerialiser(this);

		Map<String, String> term = new LinkedHashMap<>();
		term.put("t", lemma);
		if (concept == null)
			metadata.addx(language, term, "words", "languages", language, "terms");
		else
			metadata.addx(language, term, "concepts", concept.id, "languages", language, "terms");
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

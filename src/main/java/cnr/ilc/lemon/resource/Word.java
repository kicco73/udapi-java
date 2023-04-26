/**
 * @author Enrico Carniani
 */

package cnr.ilc.lemon.resource;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import cnr.ilc.rut.utils.IdGenerator;
import cnr.ilc.rut.utils.Metadata;
import cnr.ilc.sparql.TripleSerialiser;
import cnr.ilc.sparql.WordSerialiser;

public class Word implements WordInterface {
	final private String FQName;
	final public Form canonicalForm;
	final public String partOfSpeech;
	final private String language;
	final private WeakReference<ConceptInterface> concept;
	final public String lexiconFQN;
	final public String rdfType;
	final private String creator;
	final public TripleSerialiser triples;
	final public Metadata metadata = new Metadata();
	final private Collection<SenseInterface> senses = new ArrayList<>();
	final private Map<String, Form> otherForms = new LinkedHashMap<>();
	static final private IdGenerator idGenerator = new IdGenerator();

	public Word(String lemma, String partOfSpeech, String language, ConceptInterface concept, String lexiconFQN, String rdfType, String creator) {
		this.partOfSpeech = partOfSpeech;
		this.language = language;
		this.lexiconFQN = lexiconFQN;
		this.rdfType = rdfType;
		this.creator = creator;
		this.concept = concept == null? null : new WeakReference<ConceptInterface>(concept);

		String FQName = idGenerator.getId(String.format("%s+%s+%s", lemma, partOfSpeech, language));

		if (partOfSpeech != null) {
			this.FQName = String.format("term:le_%s_%s", FQName, partOfSpeech.split(":")[1]);
		} else {
			this.FQName = String.format("term:le_%s", FQName);
		}

		String canonicalFormFQN = String.format("%s_lemma", this.FQName);
		canonicalForm = new Form(canonicalFormFQN, lemma);

		this.triples = new WordSerialiser(this);

		Map<String, String> term = new LinkedHashMap<>();
		term.put("t", lemma);
		if (partOfSpeech != null) term.put("p", partOfSpeech);
		if (concept == null)
			metadata.addToList(language, term, "words", "languages", language, "terms");
		else
			metadata.addToList(language, term, "concepts", concept.getId(), "languages", language, "terms");
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

	@Override
	public String getLemma() {
		return canonicalForm.text;
	}

	@Override
	public String getPartOfSpeech() {
		return partOfSpeech;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	@Override
	public ConceptInterface getConcept() {
		return concept != null? concept.get() : null;
	}

	@Override
	public Metadata getMetadata() {
		return metadata;
	}

	@Override
	public String getSerialised() {
		return triples.serialise();
	}

	@Override
	public void addSense(SenseInterface sense) {
		senses.add(sense);
	}

	@Override
	public Collection<SenseInterface> getSenses() {
		return senses;
	}

	@Override
	public String getFQName() {
		return FQName;
	}

	@Override
	public String getCreator() {
		return creator;
	}
}

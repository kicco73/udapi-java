/**
 * @author Enrico Carniani
 */

package cnr.ilc.rut;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Word {
	public String FQName;
	public Form canonicalForm;
	public String partOfSpeech;
	public String language;
	public WeakReference<Concept> concept;
	public final Collection<Triple<String, String, String>> triples = new ArrayList<>();
	public final Collection<Triple<String, String, Map<String,String>>> tripleObjects = new ArrayList<>();
	public String lexiconFQN;
	final public Map<String, String> senses = new HashMap<>();
	final public Map<String, String> additionalInfo = new HashMap<>();
	final private Map<String, Form> otherForms = new HashMap<>();
	static final private IdGenerator idGenerator = new IdGenerator();

	public Word(String lemma, String partOfSpeech, String language, Concept concept, String lexiconFQN) {
		reuse(lemma, partOfSpeech, language, concept, lexiconFQN);
	}

	public void reuse(String lemma, String partOfSpeech, String language, Concept concept, String lexiconFQN) {
		this.partOfSpeech = partOfSpeech;
		this.language = language;
		this.lexiconFQN = lexiconFQN;

		String FQName = idGenerator.getId(String.format("%s+%s+%s", lemma, partOfSpeech, language));

		if (partOfSpeech != null) {
			this.FQName = String.format(":le_%s_%s", FQName, partOfSpeech.split(":")[1]);
		} else {
			this.FQName = String.format(":le_%s", FQName);
		}

		String canonicalFormFQN = String.format("%s_lemma", this.FQName);
		canonicalForm = new Form(canonicalFormFQN, lemma);
		otherForms.clear();
		senses.clear();
		additionalInfo.clear();
		this.concept = concept == null? null : new WeakReference<Concept>(concept);
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

	public void addFeature(String subject, String link, Map<String,String> object) {
		Triple<String, String, Map<String,String>> triple = new Triple<>(subject, link, object);
		tripleObjects.add(triple);
	}

	public void addFeatureAsUrlIfPossible(String link, String object) {
		String string = SPARQLFormatter.formatObjectWithUrlIfPossible(object);
		addFeature(link, string);
	}

	public void addFeatureAsString(String link, String object) {
		String string = SPARQLFormatter.formatObject(object);
		addFeature(link, string);
	}

	public void addFeature(String link, String object) {
		triples.add(new Triple<String,String,String>(FQName, link, object));
	}
}

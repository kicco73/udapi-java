package cnr.ilc.rut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class Concept extends SPARQLEntity {
	final public String id;
	final public String FQName;
	final public Collection<Word> words = new ArrayList<>();
	private Collection<String> subjectFields = new HashSet<>();
	static IdGenerator idGenerator = new IdGenerator();

	public Concept(String conceptId) {
		id = conceptId;
		FQName = String.format(":concept_%s", conceptId);
		addFeature(FQName, "rdf:type", "skos:Concept");
		addFeatureAsString(FQName, "skos:prefLabel", id);
	}

	public Word newWord(String lemma, String partOfSpeech, String language, String lexiconFQN) {
		Word word = new Word(lemma, partOfSpeech, language, this, lexiconFQN, "ontolex:LexicalEntry");
		words.add(word);
		return word;
	}

	public void addSubjectField(String subjectField) {
		if (!subjectFields.contains(subjectField)) {
			subjectFields.add(subjectField);
			String subjectFieldFQN = String.format("%s_%s", FQName, idGenerator.getId(subjectField));
			addFeature(subjectFieldFQN, "rdf:type", "skos:ConceptScheme");
			addFeatureAsString(subjectFieldFQN, "skos:prefLabel", subjectField);
			addFeature(FQName, "skos:inScheme", subjectFieldFQN);
		}
	}

}

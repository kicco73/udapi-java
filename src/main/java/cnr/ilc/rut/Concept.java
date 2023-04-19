package cnr.ilc.rut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import cnr.ilc.sparql.TripleSerialiser;

public class Concept {
	final public String id;
	final public String FQName;
	final public Collection<Word> words = new ArrayList<>();
	final public TripleSerialiser triples = new TripleSerialiser();
	final public Map<String,String> definition = new HashMap<>();
	private Collection<String> subjectFields = new HashSet<>();
	static IdGenerator idGenerator = new IdGenerator();

	public Concept(String conceptId) {
		id = conceptId;
		FQName = String.format(":concept_%s", conceptId);
		triples.add(FQName, "rdf:type", "skos:Concept");
		triples.addString(FQName, "skos:prefLabel", id);
	}

	public Word newWord(String lemma, String partOfSpeech, String language, String lexiconFQN, String creator) {
		Word word = new Word(lemma, partOfSpeech, language, this, lexiconFQN, "ontolex:LexicalEntry", creator);
		words.add(word);
		return word;
	}

	public void addSubjectField(String subjectField) {
		if (!subjectFields.contains(subjectField)) {
			subjectFields.add(subjectField);
			String subjectFieldFQN = String.format("%s_%s", FQName, idGenerator.getId(subjectField));
			triples.add(subjectFieldFQN, "rdf:type", "skos:ConceptScheme");
			triples.addString(subjectFieldFQN, "skos:prefLabel", subjectField);
			triples.add(FQName, "skos:inScheme", subjectFieldFQN);
		}
	}

}

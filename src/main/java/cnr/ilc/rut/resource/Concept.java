package cnr.ilc.rut.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cnr.ilc.rut.utils.IdGenerator;
import cnr.ilc.rut.utils.Metadata;
import cnr.ilc.sparql.TripleSerialiser;

public class Concept {
	final public String id;
	final public String FQName;
	final public Collection<Word> words = new ArrayList<>();
	final public TripleSerialiser triples = new TripleSerialiser();
	final private Map<String,String> definition = new HashMap<>();
	private String subjectField = null;
	final public Metadata metadata = new Metadata();
	static IdGenerator idGenerator = new IdGenerator();

	public Concept(String conceptId) {
		id = conceptId;
		FQName = String.format("conc:concept_%s", conceptId);
		triples.add(FQName, "rdf:type", "skos:Concept");
		triples.addString(FQName, "skos:prefLabel", id);
		metadata.putInMap("*", id, "concepts", id, "id");
	}

	public Word newWord(String lemma, String partOfSpeech, String language, String lexiconFQN, String creator) {
		Word word = new Word(lemma, partOfSpeech, language, this, lexiconFQN, "ontolex:LexicalEntry", creator);
		words.add(word);
		if (metadata.getObject(language, "concepts", id, "languages", language, "label") == null)
			metadata.putInMap(language, word.canonicalForm.text, "concepts", id, "languages", language, "label");

		return word;
	}

	public void setDefinition(String definition, String language) {
		this.definition.put(language, definition);
		if (language.equals("*"))
			metadata.putInMap(language, definition, "concepts", id, "definition");
		else
			metadata.putInMap(language, definition, "concepts", id, "languages", language, "definition");
	}

	public String getDefinition(String language) {
		return definition.get(language);
	}

	public void setSubjectField(String subjectField, String subjectFieldFQN) {
		this.subjectField = subjectField;
		triples.add(FQName, "skos:inScheme", subjectFieldFQN);
	}

	public String getSubjectField() {
		return subjectField;
	}
}

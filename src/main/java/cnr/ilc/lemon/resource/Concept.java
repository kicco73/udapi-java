package cnr.ilc.lemon.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cnr.ilc.rut.utils.Metadata;
import cnr.ilc.sparql.TripleSerialiser;

public class Concept implements ConceptInterface {
	final public TripleSerialiser triples = new TripleSerialiser();
	public String date = null;

	final public String id;
	final public String FQName;
	final private Collection<WordInterface> words = new ArrayList<>();
	final private Map<String,String> definition = new HashMap<>();
	final public Metadata metadata = new Metadata();
	public String subjectField = null;
	public String subjectFieldFQN = null;

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
			metadata.putInMap(language, word.getLemma(), "concepts", id, "languages", language, "label");

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
		this.subjectFieldFQN = subjectFieldFQN;
		triples.add(FQName, "skos:inScheme", subjectFieldFQN);
	}

	public String getSubjectField() {
		return subjectField;
	}

	public String getSubjectFieldFQN() {
		return subjectFieldFQN;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getFQName() {
		return FQName;
	}
	@Override
	public String getDate() {
		return date;
	}

	@Override
	public String getSerialised(String language) {
		return triples.serialise(language);
	}

	@Override
	public String getSerialised() {
		return triples.serialise();
	}

	@Override
	public String getJson() {
		return metadata.toJson("*");
	}

	@Override
	public Collection<WordInterface> getWords() {
		return words;
	}
}

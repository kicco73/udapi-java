package cnr.ilc.rut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class Concept {
	final public String FQName;
	final public String id;
	final public Collection<Word> words = new ArrayList<>();
	final public Collection<Pair<String,String>> features = new ArrayList<>();
	final public Collection<Pair<String,Map<String,String>>> featureObjects = new ArrayList<>();
	final public Collection<String> subjectFields = new HashSet<>();

	public Concept(String conceptId) {
		this.id = conceptId;
		FQName = String.format(":concept_%s", conceptId);
	}

	public Word newWord(String lemma, String partOfSpeech, String language, String lexiconFQN) {
		Word word = new Word(lemma, partOfSpeech, language, this, lexiconFQN);
		words.add(word);
		return word;
	}

	public void addFeatureAsPossibleUrl(String link, String possibleUrl) {
		Pair<String, String> pair = new Pair<>(link, SPARQLFormatter.formatObjectWithUrlIfPossible(possibleUrl));
		features.add(pair);
	}

	public void addFeatureAsStringWithLanguage(String link, String description, String language) {
		String string = SPARQLFormatter.formatObjectWithLanguage(description, language);
		addFeature(description, string);
	}

	public void addFeature(String link, String object) {
		features.add(new Pair<String,String>(link, object));
	}

	public void addFeature(String link, Map<String,String> object) {
		featureObjects.add(new Pair<String,Map<String,String>>(link, object));
	}

}

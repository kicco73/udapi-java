package cnr.ilc.rut;

import java.util.ArrayList;
import java.util.Collection;

public class Concept {
	final public String FQName;
	final public String id;
	final public Collection<Word> words = new ArrayList<>();

	public Concept(String conceptId) {
		this.id = conceptId;
		FQName = String.format(":concept_%s", conceptId);
	}

	public Word newWord(String lemma, String partOfSpeech, String language) {
		Word word = new Word(lemma, partOfSpeech, language, this);
		words.add(word);
		return word;
	}

}

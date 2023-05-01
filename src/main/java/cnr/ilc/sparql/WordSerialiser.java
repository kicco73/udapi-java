package cnr.ilc.sparql;

import java.lang.ref.WeakReference;
import java.util.Map.Entry;

import cnr.ilc.lemon.resource.SenseInterface;
import cnr.ilc.lemon.resource.Word;
import cnr.ilc.lemon.resource.TermInterface;

public class WordSerialiser extends TripleSerialiser {
	final private WeakReference<Word> word;

	public WordSerialiser(Word word) {
		super();
		this.word = new WeakReference<Word>(word);
		String wordFQN = word.getFQName();
		add(word.lexiconFQN, "lime:entry", wordFQN);       
		
		add(wordFQN, "rdf:type", word.rdfType);   
		addStringWithLanguage(wordFQN, "rdfs:label", word.canonicalForm.text, word.getLanguage());        
		if (word.getPartOfSpeech() != null)
			add(wordFQN, "lexinfo:partOfSpeech", word.getPartOfSpeech());
		addString(wordFQN, "vs:term_status", "working");
	}

	static public String serialiseLexicalSenses(TermInterface word) {
		String serialised = "";
		for(SenseInterface sense: word.getSenses()) {
			serialised += sense.getSerialised();
		}
		return serialised;
	}

	private void createCanonicalForm(Word word) {
		String canonicalFormFQN = word.canonicalForm.FQName;
		add(word.getFQName(), "ontolex:canonicalForm", canonicalFormFQN);        
		add(canonicalFormFQN, "rdf:type", "ontolex:Form");        
		addStringWithLanguage(canonicalFormFQN, "ontolex:writtenRep", word.canonicalForm.text, word.getLanguage());

		for (Entry<String,String> entry: word.canonicalForm.features.entrySet()) {
			add(canonicalFormFQN, entry.getValue(), entry.getKey());
		}

	}

	@Override
	public String serialise() {
		Word word = this.word.get();
		createCanonicalForm(word);
		return super.serialise();
	}

}

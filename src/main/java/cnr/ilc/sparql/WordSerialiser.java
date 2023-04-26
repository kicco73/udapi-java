package cnr.ilc.sparql;

import java.lang.ref.WeakReference;
import java.util.Map.Entry;

import cnr.ilc.lemon.resource.Form;
import cnr.ilc.lemon.resource.SenseInterface;
import cnr.ilc.lemon.resource.Word;
import cnr.ilc.lemon.resource.WordInterface;

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
		addMetaData(wordFQN, word.getCreator()); 
	}

	static public String serialiseLexicalSenses(WordInterface word) {
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
		addMetaData(canonicalFormFQN, word.getCreator()); 

		for (Entry<String,String> entry: word.canonicalForm.features.entrySet()) {
			add(canonicalFormFQN, entry.getValue(), entry.getKey());
		}

	}

	private void createOtherForms(Word word) {
		for (Form otherForm: word.getOtheForms()) {
			String otherFormFQN = otherForm.FQName;
			add(word.getFQName(), "ontolex:otherForm", otherFormFQN);
			add(otherFormFQN, "rdf:type", "ontolex:Form");        
			addStringWithLanguage(otherFormFQN, "ontolex:writtenRep", otherForm.text, word.getLanguage());        
			addMetaData(otherFormFQN, word.getCreator()); 

			for (Entry<String,String> entry: otherForm.features.entrySet()) {
				addString(otherFormFQN, entry.getValue(), entry.getKey());
			}
		}
	}

	@Override
	public String serialise() {
		Word word = this.word.get();
		createCanonicalForm(word);
		createOtherForms(word);
		return super.serialise();
	}

}

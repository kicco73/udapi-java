package cnr.ilc.sparql;

import java.lang.ref.WeakReference;
import java.util.Map.Entry;

import cnr.ilc.rut.resource.Form;
import cnr.ilc.rut.resource.Word;

public class WordSerialiser extends TripleSerialiser {
	final private WeakReference<Word> word;

	public WordSerialiser(Word word) {
		super();
		this.word = new WeakReference<Word>(word);
		add(word.lexiconFQN, "lime:entry", word.FQName);       
		
		add(word.FQName, "rdf:type", word.rdfType);   
		addStringWithLanguage(word.FQName, "rdfs:label", word.canonicalForm.text, word.language);        
		if (word.partOfSpeech != null)
			add(word.FQName, "lexinfo:partOfSpeech", word.partOfSpeech);
		addString(word.FQName, "vs:term_status", "working");
		addMetaData(word.FQName, word.creator); 
		}

	private void createLexicalSense(Word word, String lexicalSenseFQN, String definition) {
		add(word.FQName, "ontolex:sense", lexicalSenseFQN);        
		add(lexicalSenseFQN, "rdf:type", "ontolex:LexicalSense"); 
		if (definition != null)
			addString(lexicalSenseFQN, "skos:definition", definition);
		if (word.concept != null)
			add(lexicalSenseFQN, "ontolex:reference", word.concept.get().FQName); 
		addMetaData(lexicalSenseFQN, word.creator); 
	}

	private void createLexicalSenses(Word word) {
		if (word.senses.isEmpty()) {
			word.senses.put("", null);
		} 

		for(Entry<String, String> sense: word.senses.entrySet()) {
			String lexicalSenseFQN = String.format("%s_sense%s", word.FQName, sense.getKey());
			createLexicalSense(word, lexicalSenseFQN, sense.getValue());
		}
	}

	private void createCanonicalForm(Word word) {
		String canonicalFormFQN = word.canonicalForm.FQName;
		add(word.FQName, "ontolex:canonicalForm", canonicalFormFQN);        
		add(canonicalFormFQN, "rdf:type", "ontolex:Form");        
		addStringWithLanguage(canonicalFormFQN, "ontolex:writtenRep", word.canonicalForm.text, word.language);
		addMetaData(canonicalFormFQN, word.creator); 

		for (Entry<String,String> entry: word.canonicalForm.features.entrySet()) {
			add(canonicalFormFQN, entry.getValue(), entry.getKey());
		}

	}

	private void createOtherForms(Word word) {
		for (Form otherForm: word.getOtheForms()) {
			String otherFormFQN = otherForm.FQName;
			add(word.FQName, "ontolex:otherForm", otherFormFQN);
			add(otherFormFQN, "rdf:type", "ontolex:Form");        
			addStringWithLanguage(otherFormFQN, "ontolex:writtenRep", otherForm.text, word.language);        
			addMetaData(otherFormFQN, word.creator); 

			for (Entry<String,String> entry: otherForm.features.entrySet()) {
				addString(otherFormFQN, entry.getValue(), entry.getKey());
			}
		}
	}

	@Override
	public String serialise() {
		Word word = this.word.get();
		createLexicalSenses(word);
		createCanonicalForm(word);
		createOtherForms(word);
		return super.serialise();
	}

}

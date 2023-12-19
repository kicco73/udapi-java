package cnr.ilc.conllu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import cnr.ilc.lemon.resource.Form;
import cnr.ilc.lemon.resource.PojoWord;
import cnr.ilc.lemon.resource.Word;
import cnr.ilc.sparql.TripleSerialiser;
import cnr.ilc.lemon.resource.TermInterface;

public class WordExpander {

	static private void createOtherForms(Word word, Collection<TermInterface> collection) {
		for (Form otherForm: word.getOtheForms()) {
			TripleSerialiser triples = new TripleSerialiser();
			String otherFormFQN = otherForm.FQName;
			String language = word.getLanguage();
			triples.add(word.getFQName(), "ontolex:otherForm", otherFormFQN);
			triples.add(otherFormFQN, "rdf:type", "ontolex:Form");        
			triples.addStringWithLanguage(otherFormFQN, "ontolex:writtenRep", otherForm.text, language);

			for (Entry<String,String> entry: otherForm.features.entrySet()) {
				triples.add(otherFormFQN, entry.getKey(), entry.getValue());
			}

			TermInterface term = new PojoWord(otherForm.text, language, otherFormFQN, triples.serialise(), null);
			collection.add(term);
		}
	}

	static public Collection<TermInterface> expand(Word word) {
		Collection<TermInterface> collection = new ArrayList<>();
		collection.add(word);
		createOtherForms(word, collection);
		return collection;
	}

}

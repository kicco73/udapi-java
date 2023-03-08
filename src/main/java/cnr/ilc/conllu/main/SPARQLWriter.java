/**
 * @author Enrico Carniani
 */

package cnr.ilc.conllu.main;

import java.util.Map.Entry;

public class SPARQLWriter {
	final StringBuffer buffer = new StringBuffer();
	private boolean insertStarted = false;
	
	final private String prefixes = 
		"""		
		PREFIX : <http://tbx2rdf/test#>
		PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
		PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
		PREFIX dct: <http://purl.org/dc/terms/source>
		PREFIX lexinfo: <http://www.lexinfo.net/ontology/3.0/lexinfo#>
		PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>
		PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
		PREFIX lime: <http://www.w3.org/ns/lemon/lime#>
		""";

	private void insertTriple(String subject, String link, String object) {
		if (!insertStarted) {
			buffer.append("INSERT DATA {\n");
			insertStarted = true;
		}
		String query = String.format("\t%s %s %s .\n", subject, link, object);
		buffer.append(query);
	}

	private void createWordEntry(String lexiconFQN, Word word) {
		this.insertTriple(lexiconFQN, "lime:entry", word.FQName);        
		this.insertTriple(word.FQName, "rdf:type", "ontolex:Word");        
		this.insertTriple(word.FQName, "rdfs:label", String.format("\"%s\"@it", word.canonicalForm.text));        
		this.insertTriple(word.FQName, "lexinfo:partOfSpeech", word.partOfSpeech);
	}

	private void createLexicalSense(Word word) {
		String lexicalSenseFQN = String.format("%s_sense", word.FQName);
		this.insertTriple(word.FQName, "ontolex:sense", lexicalSenseFQN);        
		this.insertTriple(lexicalSenseFQN, "rdf:type", "ontolex:LexicalSense");        
	}

	private void createCanonicalForm(Word word) {
		String canonicalFormFQN = String.format("%s_lemma", word.FQName);
		this.insertTriple(word.FQName, "ontolex:canonicalForm", canonicalFormFQN);        
		this.insertTriple(canonicalFormFQN, "rdf:type", "ontolex:Form");        
		this.insertTriple(canonicalFormFQN, "ontolex:writtenRep", String.format("\"%s\"@it", word.canonicalForm.text));

		for (Entry<String,String> entry: word.canonicalForm.features.entrySet()) {
			this.insertTriple(canonicalFormFQN, entry.getKey(), entry.getValue());
		}

	}

	private void createOtherForms(Word word) {
		int i = 1;
		for (Form otherForm: word.getOtheForms()) {
			String otherFormFQN = String.format("%s_form%d",word.FQName, i++);
			this.insertTriple(word.FQName, "ontolex:otherForm", otherFormFQN);
			this.insertTriple(otherFormFQN, "rdf:type", "ontolex:Form");        
			this.insertTriple(otherFormFQN, "ontolex:writtenRep", String.format("\"%s\"@it", otherForm.text));        

			for (Entry<String,String> entry: otherForm.features.entrySet()) {
				this.insertTriple(otherFormFQN, entry.getKey(), entry.getValue());
			}
			}
	}
	// Hi-level interface

	public SPARQLWriter() {
		buffer.append(this.prefixes);
	}

	public String createLexicon() {
		String lexiconFQN = ":connll-u";
		
		this.insertTriple(lexiconFQN, "rdf:type", "lime:Lexicon");
        this.insertTriple(lexiconFQN, "lime:language", "\"it\"");        
		return lexiconFQN;
	}

	public void addWord(Word word, String lexiconFQN) {
		this.createWordEntry(lexiconFQN, word);
		this.createLexicalSense(word);
		this.createCanonicalForm(word);
		this.createOtherForms(word);
	}

	public void splitChunk(String separator) {
		if (insertStarted) {
			buffer.append("}\n");
			insertStarted = false;
		}
		buffer.append(String.format("# %s\n", separator));
		buffer.append(this.prefixes);
	}

	@Override
	public String toString() {
		if (insertStarted) {
			buffer.append("}\n");
			insertStarted = false;
		}
		return this.buffer.toString();
	}
}

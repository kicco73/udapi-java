/**
 * @author Enrico Carniani
 */

package cnr.ilc.rut;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

public class SPARQLWriter {
	final StringBuffer buffer = new StringBuffer();
	private boolean insertStarted = false;
	final private String creator;
	private int chunkSize;
	private int numberOfWords = 0;
	private String prefixes =
		"""		
		PREFIX : <%s>
		PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
		PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
		PREFIX dct: <http://purl.org/dc/terms/>
		PREFIX lexinfo: <http://www.lexinfo.net/ontology/3.0/lexinfo#>
		PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>
		PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
		PREFIX lime: <http://www.w3.org/ns/lemon/lime#>
		PREFIX vs: <http://www.w3.org/2003/06/sw-vocab-status/ns#>
		PREFIX vartrans: <http://www.w3.org/ns/lemon/vartrans#>
		""";

	private void splitChunk(String separator) {
		if (insertStarted) {
			buffer.append("}\n");
			insertStarted = false;
		}
		buffer.append(String.format("# %s\n%s", separator, prefixes));
	}
	
	public void insertTriple(String subject, String link, String object) {
		if (!insertStarted) {
			buffer.append("INSERT DATA {\n");
			insertStarted = true;
		}
		object = object.replaceAll("\n|\t", " ");
		object = object.replaceAll(" +", " ");
		String query = String.format("\t%s %s %s .\n", subject, link, object);
		buffer.append(query);
	}

	public void insertTriple(String subject, String link, Map<String, String> anon) {
		String object = "[ ";
		int count = anon.size();
		for (Entry<String,String> entry: anon.entrySet()) {
			object += entry.getKey() + " " + entry.getValue();
			if (--count > 0) object += " ; ";
		}
		object += " ]";
		insertTriple(subject, link, object);
	}

	private void addMetaData(String entryFQN) {
		Date now = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmX"); // Quoted "Z" to indicate UTC, no timezone offset
		String date = df.format(now);

		insertTriple(entryFQN, "dct:creator", String.format("\"%s\"", creator));
		insertTriple(entryFQN, "dct:created", String.format("\"%s:00\"", date));
		insertTriple(entryFQN, "dct:modified", String.format("\"%s:00\"", date));
	}

	private void createWordEntry(String lexiconFQN, Word word, String rdfType) {
		insertTriple(lexiconFQN, "lime:entry", word.FQName);       
		insertTriple(word.FQName, "rdf:type", rdfType);        
		insertTriple(word.FQName, "rdfs:label", String.format("\"%s\"@%s", word.canonicalForm.text, word.language));        
		if (word.partOfSpeech != null)
			insertTriple(word.FQName, "lexinfo:partOfSpeech", word.partOfSpeech);
		insertTriple(word.FQName, "vs:term_status", "\"working\"");
		addMetaData(word.FQName); 
	}

	private void createLexicalSense(Word word) {
		String lexicalSenseFQN = String.format("%s_sense", word.FQName);
		insertTriple(word.FQName, "ontolex:sense", lexicalSenseFQN);        
		insertTriple(lexicalSenseFQN, "rdf:type", "ontolex:LexicalSense"); 
		if (word.conceptFQN != null)
			insertTriple(lexicalSenseFQN, "ontolex:reference", word.conceptFQN); 
		addMetaData(lexicalSenseFQN); 
	}

	private void createCanonicalForm(Word word) {
		String canonicalFormFQN = word.canonicalForm.FQName;
		insertTriple(word.FQName, "ontolex:canonicalForm", canonicalFormFQN);        
		insertTriple(canonicalFormFQN, "rdf:type", "ontolex:Form");        
		insertTriple(canonicalFormFQN, "ontolex:writtenRep", String.format("\"%s\"@%s", word.canonicalForm.text, word.language));
		addMetaData(canonicalFormFQN); 

		for (Entry<String,String> entry: word.canonicalForm.features.entrySet()) {
			insertTriple(canonicalFormFQN, entry.getValue(), entry.getKey());
		}

	}

	private void createOtherForms(Word word) {
		for (Form otherForm: word.getOtheForms()) {
			String otherFormFQN = otherForm.FQName;
			insertTriple(word.FQName, "ontolex:otherForm", otherFormFQN);
			insertTriple(otherFormFQN, "rdf:type", "ontolex:Form");        
			insertTriple(otherFormFQN, "ontolex:writtenRep", String.format("\"%s\"@%s", otherForm.text, word.language));        
			addMetaData(otherFormFQN); 

			for (Entry<String,String> entry: otherForm.features.entrySet()) {
				insertTriple(otherFormFQN, entry.getValue(), entry.getKey());
			}
		}
	}
	// Hi-level interface

	public SPARQLWriter(String namespace, String creator, int chunkSize) {
		this.creator = creator;
		this.chunkSize = chunkSize;
		prefixes = String.format(prefixes, namespace);
		buffer.append(prefixes);
	}

	public String createLexicon(String lexiconFQN, String language) {		
		insertTriple(lexiconFQN, "rdf:type", "lime:Lexicon");
        insertTriple(lexiconFQN, "lime:language", String.format("\"%s\"", language));   
		addMetaData(lexiconFQN);     
		return lexiconFQN;
	}

	public void addWord(Word word, String lexiconFQN, String rdfType) {

		if (chunkSize > 0 && ++numberOfWords % chunkSize == 0) 
			splitChunk("[data-chunk]");

		createWordEntry(lexiconFQN, word, rdfType);
		createLexicalSense(word);
		createCanonicalForm(word);
		createOtherForms(word);
	}

	@Override
	public String toString() {
		if (insertStarted) {
			buffer.append("}\n");
			insertStarted = false;
		}
		return buffer.toString();
	}
}

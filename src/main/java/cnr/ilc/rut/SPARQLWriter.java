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
	static final public String separator = "# data-chunk";
	final private StringBuffer buffer = new StringBuffer();
	final private String creator;
	final private int chunkSize;
	final private IdGenerator idGenerator = new IdGenerator();
	private int numberOfTriples = 0;
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

	public void insertTriple(String subject, String link, String object) {
		boolean isEndOfChunk = chunkSize > 0 && numberOfTriples % chunkSize == 0;

		if (isEndOfChunk && numberOfTriples > 0) {
			if (numberOfTriples > 0)
				buffer.append(String.format("}\n%s\n%s", separator, prefixes));
		}
		if (numberOfTriples == 0 || isEndOfChunk) {
			buffer.append("INSERT DATA {\n");	
		}

		object = object.replaceAll("[\n\t ]+", " ");
		String query = String.format("\t%s %s %s .\n", subject, link, object);
		buffer.append(query);
		numberOfTriples++;
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
		Date now = DateProvider.getInstance().getDate();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmX"); // Quoted "Z" to indicate UTC, no timezone offset
		String date = df.format(now);

		insertTripleWithString(entryFQN, "dct:creator", creator);
		insertTripleWithString(entryFQN, "dct:created", date + ":00");
		insertTripleWithString(entryFQN, "dct:modified", date + ":00");
	}

	private void createWordEntry(Word word, String rdfType) {
		insertTriple(word.lexiconFQN, "lime:entry", word.FQName);       
		
		insertTriple(word.FQName, "rdf:type", rdfType);   
		insertTripleWithLanguage(word.FQName, "rdfs:label", word.canonicalForm.text, word.language);        
		if (word.partOfSpeech != null)
			insertTriple(word.FQName, "lexinfo:partOfSpeech", word.partOfSpeech);
		insertTripleWithString(word.FQName, "vs:term_status", "working");

		for (Entry<String,String> entry: word.additionalInfo.entrySet()) {
			insertTripleWithString(word.FQName, entry.getKey(), entry.getValue());
		}
	
		addMetaData(word.FQName); 
	}

	private void createLexicalSense(Word word, String lexicalSenseFQN, String definition) {
		insertTriple(word.FQName, "ontolex:sense", lexicalSenseFQN);        
		insertTriple(lexicalSenseFQN, "rdf:type", "ontolex:LexicalSense"); 
		if (definition != null)
			insertTripleWithString(lexicalSenseFQN, "skos:definition", definition);
		if (word.concept != null)
			insertTriple(lexicalSenseFQN, "ontolex:reference", word.concept.get().FQName); 
		addMetaData(lexicalSenseFQN); 
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
		insertTriple(word.FQName, "ontolex:canonicalForm", canonicalFormFQN);        
		insertTriple(canonicalFormFQN, "rdf:type", "ontolex:Form");        
		insertTripleWithLanguage(canonicalFormFQN, "ontolex:writtenRep", word.canonicalForm.text, word.language);
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
			insertTripleWithLanguage(otherFormFQN, "ontolex:writtenRep", otherForm.text, word.language);        
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
        insertTripleWithString(lexiconFQN, "lime:language", language);   
		addMetaData(lexiconFQN);     
		return lexiconFQN;
	}

	private void createFeatures(Word word) {		
		for (Triple<String, String, String> feature: word.triples) {
			insertTriple(feature.first, feature.second, (String) feature.third);
		}
		for (Triple<String, String, Map<String,String>> feature: word.tripleObjects) {
			if (feature.third instanceof Map)
			insertTriple(feature.first, feature.second, feature.third);
		}
	}

	public void addWord(Word word, String rdfType) {
		createWordEntry(word, rdfType);
		createLexicalSenses(word);
		createCanonicalForm(word);
		createOtherForms(word);
		createFeatures(word);
	}

	public void addConcept(Concept concept) {
		insertTriple(concept.FQName, "rdf:type", "skos:Concept");
		insertTripleWithString(concept.FQName, "skos:prefLabel", concept.id);

		for (Pair<String, String> feature: concept.features) {
			insertTriple(concept.FQName, feature.first, feature.second);
		}

		for (Pair<String, Map<String,String>> feature: concept.featureObjects) {
			insertTriple(concept.FQName, feature.first, (Map<String,String>) feature.second);
		}

		for (String subjectField: concept.subjectFields) {
			String subjectFieldFQN = String.format("%s_%s", concept.FQName, idGenerator.getId(subjectField));
			insertTriple(subjectFieldFQN, "rdf:type", "skos:ConceptScheme");
			insertTripleWithString(subjectFieldFQN, "skos:prefLabel", subjectField);
			insertTriple(concept.FQName, "skos:inScheme", subjectFieldFQN);
		}
	}

	public void insertTripleWithUrlIfPossible(String subject, String link, String object) {
		insertTriple(subject, link, SPARQLFormatter.formatObjectWithUrlIfPossible(object));
	}

	public void insertTripleWithLanguage(String subject, String link, String object, String language) {
		insertTriple(subject, link, SPARQLFormatter.formatObjectWithLanguage(object, language));
	}

	public void insertTripleWithString(String subject, String link, String object) {
		insertTriple(subject, link, SPARQLFormatter.formatObject(object));
	}

	@Override
	public String toString() {
		if (numberOfTriples % chunkSize != 0) {
			buffer.append("}\n");
		}
		return buffer.toString();
	}
}

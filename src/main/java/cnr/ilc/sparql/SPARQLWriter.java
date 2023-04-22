/**
 * @author Enrico Carniani
 */

package cnr.ilc.sparql;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

import cnr.ilc.rut.Concept;
import cnr.ilc.rut.Metadata;
import cnr.ilc.rut.ResourceInterface;
import cnr.ilc.rut.Word;

@SuppressWarnings("unchecked")
public class SPARQLWriter implements TripleStoreInterface {
	static final public String separator = "# data-chunk";
	final private StringBuffer buffer = new StringBuffer();
	final protected String creator;
	final private int chunkSize;
	private int charsWritten = 0;
	private boolean blockStarted = false;
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

	private void appendLine(String block) {
		boolean isEndOfChunk = charsWritten > chunkSize;

		if (isEndOfChunk) {
			buffer.append(String.format("}\n%s\n%s", separator, prefixes));
			charsWritten = 0;
			blockStarted = false;
		}
		if (charsWritten == 0 || isEndOfChunk) {
			buffer.append("INSERT DATA {\n");	
			blockStarted = true;
		}

		buffer.append(block);
		charsWritten += block.length();
	}

	protected void append(String block) {
		for (String line: block.split("\n")) {
			appendLine(line+"\n");
		}
	}

	private void appendLexicons(Map<String, String> lexicons) {
		for (Entry<String, String> lexicon: lexicons.entrySet()) {
			String language = lexicon.getKey();
			String lexiconFQN = lexicon.getValue();
			appendLexicon(lexiconFQN, language);
		}
	}

	private void appendConcepts(ResourceInterface resource) {
		if (resource.getConcepts() == null) return;
		for (Concept concept: resource.getConcepts()) {
			appendConcept(concept, resource.getLexicons().keySet());
			for (Word word: concept.words) 
				appendWord(word);
		}
	}

	private void appendWords(ResourceInterface resource) {
		if (resource.getWords() == null) return;
		for (Word word: resource.getWords()) {
			appendWord(word);
		}
	}

	protected void appendLexicon(String lexiconFQN, String language) {		
		TripleSerialiser triples = new TripleSerialiser();
		triples.addLexicon(lexiconFQN, language, creator);
		append(triples.serialise());
	}

	protected void appendConcept(Concept concept, Collection<String> languages) {
		append(concept.triples.serialise());
	}

	protected void appendWord(Word word) {
		append(word.triples.serialise());
	}

	// Hi-level interface

	public SPARQLWriter(String namespace, String creator, int chunkSize) {
		this.creator = creator;
		this.chunkSize = chunkSize;
		prefixes = String.format(prefixes, namespace);
		buffer.append(prefixes);
	}

	@Override
	public void serialise(ResourceInterface resource) {
		appendLexicons(resource.getLexicons());
        appendConcepts(resource);
		appendWords(resource);
	}
	
	@Override
	public String serialised() {
		if (blockStarted) {
			buffer.append("}\n");
		}
		return buffer.toString();
	}

	@Override
	public Map<String, Object> getMetadata(ResourceInterface resource) {
		Metadata coalescedMetadata = new Metadata();
		Collection<String> languages = resource.getLexicons().keySet();

		coalescedMetadata.addx("*", languages, "languages");
		languages = new HashSet<>(languages);
		languages.add("*");

		for (Concept concept: resource.getConcepts()) {
			for (String language: languages) {
				Map<String,Object> conceptData = (Map<String, Object>) concept.metadata.getx(language);
				if (conceptData != null)
					coalescedMetadata.merge("*", conceptData);
				for (Word word: concept.words) {
					Map<String,Object> wordData = (Map<String, Object>) word.metadata.getx(language);
					if (wordData != null)
						coalescedMetadata.merge("*", wordData);
				}
			}
		}
		Map<String, Object> unwrapped = (Map<String, Object>) coalescedMetadata.getx("*");
		return unwrapped;
	}
}

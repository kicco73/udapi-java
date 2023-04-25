/**
 * @author Enrico Carniani
 */

package cnr.ilc.stores;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import cnr.ilc.rut.resource.Concept;
import cnr.ilc.rut.resource.Global;
import cnr.ilc.rut.resource.ResourceInterface;
import cnr.ilc.rut.resource.Word;
import cnr.ilc.sparql.TripleSerialiser;

public class MemoryStore implements TripleStoreInterface {
	static final public String separator = "# data-chunk";
	final protected String creator;
	final private int chunkSize;
	final private StringBuffer buffer = new StringBuffer();
	private Map<String, Object> metadata = null;
	private int charsWritten = 0;
	private boolean blockStarted = false;
	private String prefixes =
		"""		
		PREFIX : <%s>
		PREFIX conc: <%s>
		PREFIX term: <%s>
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

	private void appendGlobals(ResourceInterface resource) throws Exception {
		if (resource.getGlobals() == null) return;
		for (Global global: resource.getGlobals()) {
			appendGlobal(global);
		}
	}

	private void appendConcepts(ResourceInterface resource) throws Exception {
		if (resource.getConcepts() == null) return;
		for (Concept concept: resource.getConcepts()) {
			appendConcept(concept, resource.getLanguages());
			for (Word word: concept.words) 
				appendWord(word);
		}
	}

	private void appendWords(ResourceInterface resource) throws Exception {
		if (resource.getWords() == null) return;
		for (Word word: resource.getWords()) {
			appendWord(word);
		}
	}

	protected void appendGlobal(Global global) throws Exception {		
		append(global.triples.serialise());
	}

	protected void appendConcept(Concept concept, Collection<String> languages) throws Exception {
		append(concept.triples.serialise());
	}

	protected void appendWord(Word word) throws Exception {
		append(word.triples.serialise());
	}

	// Hi-level interface

	public MemoryStore(String namespace, String creator, int chunkSize) {
		this.creator = creator;
		this.chunkSize = chunkSize;
		prefixes = String.format(prefixes, namespace, namespace, namespace);
		buffer.append(prefixes);
	}

	@Override
	public void store(ResourceInterface resource) throws Exception {
		appendGlobals(resource);
		appendConcepts(resource);
		appendWords(resource);
	}
	
	@Override
	public String getSparql() throws Exception {
		if (blockStarted) {
			buffer.append("}\n");
		}
		return buffer.toString();
	}

	@Override
	public Map<String, Object> getMetadata() throws Exception {
		return metadata;
	}
}

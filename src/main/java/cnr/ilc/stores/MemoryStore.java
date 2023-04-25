/**
 * @author Enrico Carniani
 */

package cnr.ilc.stores;

import java.util.Collection;
import java.util.Map;
import cnr.ilc.rut.resource.Concept;
import cnr.ilc.rut.resource.Global;
import cnr.ilc.rut.resource.ResourceInterface;
import cnr.ilc.rut.resource.Word;
import cnr.ilc.sparql.SPARQLWriter;

public class MemoryStore implements TripleStoreInterface {
	protected SPARQLWriter output;

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
		output.append(global.triples.serialise());
	}

	protected void appendConcept(Concept concept, Collection<String> languages) throws Exception {
		output.append(concept.triples.serialise());
	}

	protected void appendWord(Word word) throws Exception {
		output.append(word.triples.serialise());
	}

	// Hi-level interface

	public MemoryStore(String namespace, String creator, int chunkSize) {
		output = new SPARQLWriter(namespace, creator, chunkSize);
	}

	@Override
	public void store(ResourceInterface resource) throws Exception {
		appendGlobals(resource);
		appendConcepts(resource);
		appendWords(resource);
	}
	
	@Override
	public String getSparql() throws Exception {
		return output.getSparql();
	}

	@Override
	public Map<String, Object> getMetadata() throws Exception {
		return null;
	}
}

/**
 * @author Enrico Carniani
 */

package cnr.ilc.stores;

import java.util.Collection;
import java.util.Map;

import cnr.ilc.lemon.resource.ConceptInterface;
import cnr.ilc.lemon.resource.Global;
import cnr.ilc.lemon.resource.ResourceInterface;
import cnr.ilc.lemon.resource.WordInterface;
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
		for (ConceptInterface concept: resource.getConcepts()) {
			appendConcept(concept, resource.getLanguages());
			for (WordInterface word: concept.getWords()) 
				appendWord(word);
		}
	}

	private void appendWords(ResourceInterface resource) throws Exception {
		if (resource.getWords() == null) return;
		for (WordInterface word: resource.getWords()) {
			appendWord(word);
		}
	}

	protected void appendGlobal(Global global) throws Exception {		
		output.append(global.triples.serialise());
	}

	protected void appendConcept(ConceptInterface concept, Collection<String> languages) throws Exception {
		output.append(concept.getSerialised());
	}

	protected void appendWord(WordInterface word) throws Exception {
		output.append(word.getSerialised());
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

/**
 * @author Enrico Carniani
 */

package cnr.ilc.stores;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import cnr.ilc.lemon.resource.ConceptInterface;
import cnr.ilc.lemon.resource.Global;
import cnr.ilc.lemon.resource.ResourceInterface;
import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.sparql.SPARQLWriter;
import cnr.ilc.sparql.TripleSerialiser;
import cnr.ilc.sparql.WordSerialiser;
import cnr.ilc.stores.filterstore.Filter;
import cnr.ilc.processors.DummyProcessor;
import cnr.ilc.processors.PostProcessor;
import cnr.ilc.processors.ProcessorInterface;

public class MemoryStore implements TripleStoreInterface {
	final protected SPARQLWriter output;
	final protected ProcessorInterface processor;
	private Collection<WordInterface> words = new ArrayList<WordInterface>();

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
			appendWords(concept.getWords());
		}
	}

	private void appendWords(Collection<WordInterface> words) throws Exception {
		if (words == null) return;
		this.words.addAll(words);
	}

	protected void appendGlobal(Global global) throws Exception {		
		output.append(global.triples.serialise());
	}

	protected void appendConcept(ConceptInterface concept, Collection<String> languages) throws Exception {
		output.append(concept.getSerialised());
	}

	protected void appendWord(WordInterface word) throws Exception {
		output.append(word.getSerialised());
		output.append(WordSerialiser.serialiseLexicalSenses(word));

	}

	protected void finaliseStore() throws Exception {
		TripleSerialiser tripleSerialiser = new TripleSerialiser();
		words = processor.filter(words, tripleSerialiser);
	}

	// Hi-level interface

	public MemoryStore(String namespace, String creator, int chunkSize, Filter filter) {
		output = new SPARQLWriter(namespace, creator, chunkSize);
		processor = filter != null? new PostProcessor(filter) : new DummyProcessor();
	}

	@Override
	public void store(ResourceInterface resource) throws Exception {
		appendGlobals(resource);
		appendConcepts(resource);
		appendWords(resource.getWords());

		finaliseStore();

		for (WordInterface word: words)
			appendWord(word);
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

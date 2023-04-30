package cnr.ilc.sparql;

/**
 * @author Enrico Carniani
 */

import java.util.Collection;

import cnr.ilc.lemon.resource.ConceptInterface;
import cnr.ilc.lemon.resource.GlobalInterface;
import cnr.ilc.lemon.resource.ResourceInterface;
import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.processors.PostProcessor;
import cnr.ilc.processors.ProcessorInterface;
import cnr.ilc.rut.Filter;
import cnr.ilc.rut.utils.Logger;

public class SPARQLAssembler {
	final private ProcessorInterface postProcessor;
	final private SPARQLWriter output;

	private void processGlobals(ResourceInterface resource) throws Exception {
		if (resource.getGlobals() == null)
			return;
		for (GlobalInterface global : resource.getGlobals()) {
			output.append(global.getSerialised());
		}
	}

	private void processConcepts(ResourceInterface resource) throws Exception {
		if (resource.getConcepts() == null)
			return;
		for (ConceptInterface concept: resource.getConcepts()) {
			output.append(concept.getSerialised());
		}
	}

	private void processWords(ResourceInterface resource) throws Exception {
		TripleSerialiser triples = new TripleSerialiser();

		Collection<WordInterface> words = resource.getWords();
		words = postProcessor.process(words, triples);

		int count = 0;
		for (WordInterface word: words) {
			Logger.progress(count++ * 100 / words.size(), "Assemble completed");

			output.append(word.getSerialised());
			output.append(WordSerialiser.serialiseLexicalSenses(word));
		}

		output.append(triples.serialise());	
		Logger.progress(100, "Assemble completed");
	}

	// Hi-level interface

	public SPARQLAssembler(String namespace, String creator, int chunkSize, Filter filter) {
		output = new SPARQLWriter(namespace, creator, chunkSize);
		postProcessor = PostProcessor.make(filter, creator);
	}

	public void serialise(ResourceInterface resource) throws Exception {
		processGlobals(resource);
		processConcepts(resource);
		processWords(resource);
	}

	public String getSparql() throws Exception {
		return output.getSparql();
	}
}

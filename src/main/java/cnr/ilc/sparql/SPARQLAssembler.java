package cnr.ilc.sparql;

/**
 * @author Enrico Carniani
 */

import java.util.Collection;

import cnr.ilc.lemon.resource.ConceptInterface;
import cnr.ilc.lemon.resource.GlobalInterface;
import cnr.ilc.lemon.resource.ResourceInterface;
import cnr.ilc.lemon.resource.TermInterface;
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
		output.append("\n\t# Concept section\n");
		for (ConceptInterface concept: resource.getConcepts()) {
			output.append(concept.getSerialised());
		}
	}

	private void processTerms(ResourceInterface resource) throws Exception {
		TripleSerialiser triples = new TripleSerialiser();

		Collection<TermInterface> words = resource.getTerms();
		words = postProcessor.process(words, triples);

		output.append("\n\t# Term section\n");

		int count = 0;
		for (TermInterface word: words) {
			Logger.progress(count++ * 100 / words.size(), "Assembling");

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
		processTerms(resource);
	}

	public String getSparql() throws Exception {
		return output.getSparql();
	}
}

package cnr.ilc.sparql;

/**
 * @author Enrico Carniani
 */

import java.util.ArrayList;
import java.util.Collection;

import cnr.ilc.lemon.resource.ConceptInterface;
import cnr.ilc.lemon.resource.Global;
import cnr.ilc.lemon.resource.GlobalInterface;
import cnr.ilc.lemon.resource.ResourceInterface;
import cnr.ilc.lemon.resource.WordInterface;

public class SPARQLSerialiser {
	final protected SPARQLWriter output;
	private Collection<WordInterface> words = new ArrayList<WordInterface>();

	private void appendGlobals(ResourceInterface resource) throws Exception {
		if (resource.getGlobals() == null)
			return;
		for (GlobalInterface global : resource.getGlobals()) {
			appendGlobal(global);
		}
	}

	private void appendConcepts(ResourceInterface resource) throws Exception {
		if (resource.getConcepts() == null)
			return;
		for (ConceptInterface concept : resource.getConcepts()) {
			appendConcept(concept, resource.getLanguages());
			appendWords(concept.getWords());
		}
	}

	private void appendWords(Collection<WordInterface> words) throws Exception {
		if (words == null)
			return;
		this.words.addAll(words);
	}

	protected void appendGlobal(GlobalInterface global) throws Exception {
		output.append(global.getSerialised());
	}

	protected void appendConcept(ConceptInterface concept, Collection<String> languages) throws Exception {
		output.append(concept.getSerialised());
	}

	protected void appendWord(WordInterface word) throws Exception {
		output.append(word.getSerialised());
		output.append(WordSerialiser.serialiseLexicalSenses(word));
	}

	// Hi-level interface

	public SPARQLSerialiser(String namespace, String creator, int chunkSize) {
		output = new SPARQLWriter(namespace, creator, chunkSize);
	}

	public void serialise(ResourceInterface resource) throws Exception {
		appendGlobals(resource);
		appendConcepts(resource);
		appendWords(resource.getWords());

		for (WordInterface word : words)
			appendWord(word);
	}

	public String getSparql() throws Exception {
		return output.getSparql();
	}
}

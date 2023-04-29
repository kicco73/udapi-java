package cnr.ilc.processors;

import java.util.Collection;

import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.sparql.TripleSerialiser;

public interface ProcessorInterface {
	Collection<WordInterface> process(Collection<WordInterface> words, TripleSerialiser triples);
}

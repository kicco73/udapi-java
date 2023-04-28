package cnr.ilc.stores.filterstore.processors;

import java.util.Collection;

import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.sparql.TripleSerialiser;

public interface ProcessorInterface {
	Collection<WordInterface> filter(Collection<WordInterface> words, TripleSerialiser triples);
}

package cnr.ilc.processors;

import java.util.Collection;

import cnr.ilc.lemon.resource.TermInterface;
import cnr.ilc.sparql.TripleSerialiser;

public interface ProcessorInterface {
	Collection<TermInterface> process(Collection<TermInterface> words, TripleSerialiser triples);
}

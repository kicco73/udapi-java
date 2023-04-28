package cnr.ilc.processors;

import java.util.Collection;

import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.sparql.TripleSerialiser;

public class DummyProcessor implements ProcessorInterface {

    @Override
    public Collection<WordInterface> filter(Collection<WordInterface> words, TripleSerialiser triples) {
		  return words;
    }
    
}   

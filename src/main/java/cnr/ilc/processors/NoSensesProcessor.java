package cnr.ilc.processors;

import java.util.Collection;

import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.sparql.TripleSerialiser;
public class NoSensesProcessor implements ProcessorInterface {

    @Override
    public Collection<WordInterface> filter(Collection<WordInterface> words, TripleSerialiser triples)  {
		triples.addComment("[No Sense Processor] Connecting terms to concepts via ontolex:denotes relationship");

        for (WordInterface word: words) {
            word.getSenses().clear();            
			String FQName = word.getFQName();
			String conceptFQN = word.getConceptFQN();
			triples.add(FQName, "ontolex:denotes", conceptFQN);
        }

        return words;
    }
}   

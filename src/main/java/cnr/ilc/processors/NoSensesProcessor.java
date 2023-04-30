package cnr.ilc.processors;

import java.util.Collection;

import cnr.ilc.lemon.resource.TermInterface;
import cnr.ilc.sparql.TripleSerialiser;
public class NoSensesProcessor implements ProcessorInterface {

    @Override
    public Collection<TermInterface> process(Collection<TermInterface> words, TripleSerialiser triples)  {
		triples.addComment("[No Sense Processor] Connecting terms to concepts via ontolex:denotes relationship");

        for (TermInterface word: words) {
            word.getSenses().clear();            
			String FQName = word.getFQName();
			String conceptFQN = word.getConceptFQN();
			triples.add(FQName, "ontolex:denotes", conceptFQN);
        }

        return words;
    }
}   

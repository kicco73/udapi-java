package cnr.ilc.stores.filterstore.processors;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import cnr.ilc.lemon.resource.SenseInterface;
import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.rut.utils.Metadata;
import cnr.ilc.sparql.TripleSerialiser;

@SuppressWarnings("unchecked")
public class SynonymsProcessor implements ProcessorInterface {

    private void createSynonymsAmongAllSenses(Collection<SenseInterface> senses, TripleSerialiser triples) {
        SenseInterface origin = null;
        for (SenseInterface destination: senses) {
            if (origin != null) {
                triples.add(origin.getFQName(), "ontolex:synonym", destination.getFQName());
            }
            origin = destination;
        }
    }

    @Override
    public Collection<WordInterface> filter(Collection<WordInterface> words, TripleSerialiser triples) {
        Metadata metadata = new Metadata();
        
        for (WordInterface word: words) {
            for (SenseInterface sense: word.getSenses())
                metadata.addToList(sense.getConceptFQN(), sense);
        }

        Map<String, Object> concepts = metadata.getRoot();
        for (Entry<String, Object> concept: concepts.entrySet()) {
            String conceptFQN = concept.getKey();
            Collection<SenseInterface> senses = (Collection<SenseInterface>) concept.getValue();
            if (senses.size() > 1) triples.addComment("[Synonyms Processor] generating sense synonyms for concept `%s`", conceptFQN);
            createSynonymsAmongAllSenses(senses, triples);    
        }
        return words;
    }
    
}   

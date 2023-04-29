package cnr.ilc.processors;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

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

    private Map<String, Collection<SenseInterface>> groupSensesByConcept(Collection<WordInterface> words) {
        Metadata metadata = new Metadata();

        for (WordInterface word: words) {
            for (SenseInterface sense: word.getSenses())
                metadata.addToList(sense.getConceptFQN(), sense);
        }

        return (JSONObject) metadata.getRoot();
    }

    @Override
    public Collection<WordInterface> process(Collection<WordInterface> words, TripleSerialiser triples) {
        Map<String, Collection<SenseInterface>> concepts = groupSensesByConcept(words);
        
        for (Entry<String, Collection<SenseInterface>> concept: concepts.entrySet()) {
            String conceptFQN = concept.getKey();
            Collection<SenseInterface> senses = (Collection<SenseInterface>) concept.getValue();
            if (senses.size() > 1) triples.addComment("[Synonyms Processor] generating sense synonyms for concept `%s`", conceptFQN);
            createSynonymsAmongAllSenses(senses, triples);    
        }
        return words;
    }
    
}   

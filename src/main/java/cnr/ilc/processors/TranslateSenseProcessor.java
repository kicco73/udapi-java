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
public class TranslateSenseProcessor implements ProcessorInterface {

    private void createTranslationAmongAllSenses(Collection<SenseInterface> senses, TripleSerialiser triples) {
        SenseInterface origin = null;
        for (SenseInterface destination: senses) {
            if (origin != null) {
                triples.add(origin.getFQName(), "lexinfo:translation", destination.getFQName());
            }
            origin = destination;
        }
    }

    private Map<String, Collection<SenseInterface>> groupSensesByConceptAndLanguage(Collection<WordInterface> words) {
        Metadata metadata = new Metadata();

        for (WordInterface word: words) {
            SenseInterface sense = word.getSenses().iterator().next(); // FIXME:
            metadata.putInMap(word.getConceptFQN(), sense, word.getLanguage());
        }

        Metadata result = new Metadata();
        
        for (Entry<String, Object> entry: metadata.getRoot().entrySet()) {
            String conceptFQN = entry.getKey();
            Map<String, SenseInterface> languages = (JSONObject) entry.getValue();
            result.putInMap(conceptFQN, languages.values());
        }

        return (JSONObject) result.getRoot();
    }


    @Override
    public Collection<WordInterface> process(Collection<WordInterface> words, TripleSerialiser triples) {
        Map<String, Collection<SenseInterface>> concepts = groupSensesByConceptAndLanguage(words);
        
        for (Entry<String, Collection<SenseInterface>> concept: concepts.entrySet()) {
            String conceptFQN = concept.getKey();
            Collection<SenseInterface> senses = concept.getValue();
            if (senses.size() > 1) triples.addComment("[Translate Sense Processor] generating sense translations for concept `%s`", conceptFQN);
            createTranslationAmongAllSenses(senses, triples);    
        }
        return words;
    }
    
}   

package cnr.ilc.processors;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.rut.utils.Metadata;
import cnr.ilc.sparql.TripleSerialiser;

@SuppressWarnings("unchecked")
public class TranslateTermProcessor implements ProcessorInterface {

    private void createTranslationAmongAllWords(Collection<WordInterface> words, TripleSerialiser triples) {
        WordInterface origin = null;
        for (WordInterface destination: words) {
            if (origin != null) {
                triples.add(origin.getFQName(), "vartrans:translatableAs", destination.getFQName());
            }
            origin = destination;
        }
    }

    private Map<String, Collection<WordInterface>> groupWordsByConceptAndLanguage(Collection<WordInterface> words) {
        Metadata metadata = new Metadata();

        for (WordInterface word: words) {
            metadata.putInMap(word.getConceptFQN(), word, word.getLanguage());
        }

        Metadata result = new Metadata();
        
        for (Entry<String, Object> entry: metadata.getRoot().entrySet()) {
            String conceptFQN = entry.getKey();
            Map<String, WordInterface> languages = (JSONObject) entry.getValue();
            result.putInMap(conceptFQN, languages.values());
        }

        return (JSONObject) result.getRoot();
    }

    @Override
    public Collection<WordInterface> process(Collection<WordInterface> words, TripleSerialiser triples) {
        Map<String, Collection<WordInterface>> concepts = groupWordsByConceptAndLanguage(words);
        
        for (Entry<String, Collection<WordInterface>> concept: concepts.entrySet()) {
            String conceptFQN = concept.getKey();
            Collection<WordInterface> terms = concept.getValue();
            if (terms.size() > 1) triples.addComment("[Translate Term Processor] generating term translations for concept `%s`", conceptFQN);
            createTranslationAmongAllWords(terms, triples);    
        }
        return words;
    }
    
}   

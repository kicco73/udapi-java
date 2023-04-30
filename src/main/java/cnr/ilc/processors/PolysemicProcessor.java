package cnr.ilc.processors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;

import cnr.ilc.lemon.PolysemicResolver;
import cnr.ilc.lemon.resource.TermInterface;
import cnr.ilc.rut.utils.Metadata;
import cnr.ilc.sparql.TripleSerialiser;

@SuppressWarnings("unchecked")
public class PolysemicProcessor implements ProcessorInterface {
    PolysemicResolver resolver = new PolysemicResolver();

    @Override
    public Collection<TermInterface> process(Collection<TermInterface> words, TripleSerialiser triples)  {
        Collection<TermInterface> result = new ArrayList<>();
        Metadata metadata = new Metadata();

		for (TermInterface word: words) {
            metadata.addToList(word.getLanguage(), word, word.getLemma());
        }

        Collection<String> languages = metadata.getRoot().keySet();

        for (String language: languages) {
            Map<String, Object> map = metadata.getMap(language);
            for (Entry<String, Object> group: map.entrySet()) {
                String term = group.getKey();
                Collection<TermInterface> wordSet = (JSONArray) group.getValue();
                Collection<TermInterface> replacementSet = resolver.resolve(wordSet);
                result.addAll(replacementSet);

                if (wordSet.size() > 1) {
                    // TODO: this comment is assuming the resolver merges all senses into one term
                    triples.addComment("[Polysemic Processor] term `%s`@%s merged with %d senses", term, language, wordSet.size());
                }
            }
        }
        return result;
    }
}   

package cnr.ilc.processors;

import java.util.Collection;

import cnr.ilc.lemon.resource.SenseInterface;
import cnr.ilc.lemon.resource.TermInterface;
import cnr.ilc.sparql.TripleSerialiser;

public class AddMetadataProcessor implements ProcessorInterface {
    final private String creator;

    public AddMetadataProcessor(String creator) {
        this.creator = creator;
    }

    @Override
    public Collection<TermInterface> process(Collection<TermInterface> words, TripleSerialiser triples)  {
		triples.addComment("[Add Metadata Processor] adding creator and date info to words and senses");

        for (TermInterface word: words) {
            triples.addMetaData(word.getFQName(), creator);
            for (SenseInterface sense: word.getSenses()) {
                triples.addMetaData(sense.getFQName(), creator);
            }
        }

        return words;
    }
}   

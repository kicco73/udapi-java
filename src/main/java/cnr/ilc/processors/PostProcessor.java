package cnr.ilc.processors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.rut.Filter;
import cnr.ilc.sparql.TripleSerialiser;

public class PostProcessor implements ProcessorInterface {
    private List<ProcessorInterface> processors = new ArrayList<ProcessorInterface>();

	static public PostProcessor make(Filter filter, String creator) {
		PostProcessor postProcessor = new PostProcessor();

		if (filter != null) {
			if (filter.isNoSenses()) {
				postProcessor.processors.add(new NoSensesProcessor());
				if (filter.isTranslateTerms())
					postProcessor.processors.add(new TranslateTermProcessor());
			} else {
				postProcessor.processors.add(new PolysemicProcessor());
				if (filter.isTranslateSenses())
					postProcessor.processors.add(new TranslateSenseProcessor());
				if (filter.isSynonyms())
					postProcessor.processors.add(new SynonymsProcessor());
			}
		}
		
		postProcessor.processors.add(new AddMetadataProcessor(creator));
		return postProcessor;
	}

    @Override
    public Collection<WordInterface> process(Collection<WordInterface> words, TripleSerialiser triples) {
		for (ProcessorInterface processor: processors)
            words = processor.process(words, triples);
        return words;
    }
    
}   

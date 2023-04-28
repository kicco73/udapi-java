package cnr.ilc.processors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.sparql.TripleSerialiser;
import cnr.ilc.stores.filterstore.Filter;

public class PostProcessor implements ProcessorInterface {
    final private List<ProcessorInterface> processors;

	static private List<ProcessorInterface> buildPipeline(Filter filter) {
		List<ProcessorInterface> processors = new ArrayList<>();
		if (filter.isNoSenses()) {
			processors.add(new NoSensesProcessor());
			if (filter.isTranslateTerms())
				processors.add(new TranslateTermProcessor());
		} else {
			processors.add(new PolysemicProcessor());
			if (filter.isTranslateSenses())
				processors.add(new TranslateSenseProcessor());
			if (filter.isSynonyms())
				processors.add(new SynonymsProcessor());
		}
		return processors;
	}

    public PostProcessor(Filter filter) {
        processors = buildPipeline(filter);
    }

    @Override
    public Collection<WordInterface> filter(Collection<WordInterface> words, TripleSerialiser triples) {
		for (ProcessorInterface processor: processors)
            words = processor.filter(words, triples);
        return words;
    }
    
}   

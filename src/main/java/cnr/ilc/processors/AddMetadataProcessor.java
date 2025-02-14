package cnr.ilc.processors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cnr.ilc.lemon.resource.SenseInterface;
import cnr.ilc.lemon.resource.TermInterface;
import cnr.ilc.rut.utils.Logger;
import cnr.ilc.sparql.TripleSerialiser;

public class AddMetadataProcessor implements ProcessorInterface {
    final private String creator;
    final private boolean excludeConcepts;

    public AddMetadataProcessor(String creator, boolean excludeConcepts) {
        this.creator = creator;
        this.excludeConcepts = excludeConcepts;
    }

    @Override
    public Collection<TermInterface> process(Collection<TermInterface> terms, TripleSerialiser triples)  {
        Set<String> conceptFQNs = new HashSet<>();
        Set<String> languages = new HashSet<>();

		triples.addComment("[Add Metadata Processor] adding creator and date info");
        int total = terms.size();
        int current = 0;
        for (TermInterface term: terms) {
            int progress = 100 * current++/total;
            Logger.progress(progress, "Adding metadata");

            String conceptFQN = term.getConceptFQN();
            if (!excludeConcepts && conceptFQN != null && !conceptFQNs.contains(conceptFQN)) {
                triples.addMetaData(conceptFQN, creator);
                conceptFQNs.add(conceptFQN);
            }
            String language = term.getLanguage();
            if (!languages.contains(language)) {
                triples.addMetaData(TripleSerialiser.getLexiconFQN(language), creator);
                languages.add(language);
            }

            triples.addMetaData(term.getFQName(), creator);
            for (SenseInterface sense: term.getSenses()) {
                triples.addMetaData(sense.getFQName(), creator);
            }
        }
        Logger.progress(100, "Done");

        return terms;
    }
}   

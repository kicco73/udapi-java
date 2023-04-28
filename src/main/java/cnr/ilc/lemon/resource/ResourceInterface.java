package cnr.ilc.lemon.resource;

import java.util.Collection;

public interface ResourceInterface {
	Collection<String> getLanguages();
	Collection<GlobalInterface> getGlobals();
	Collection<ConceptInterface> getConcepts();
	Collection<WordInterface> getWords();
}

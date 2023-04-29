package cnr.ilc.lemon.resource;

import java.util.Collection;

public interface ResourceInterface {
	Collection<String> getLanguages() throws Exception;
	Collection<GlobalInterface> getGlobals() throws Exception;
	Collection<ConceptInterface> getConcepts() throws Exception;
	Collection<WordInterface> getWords() throws Exception;
}

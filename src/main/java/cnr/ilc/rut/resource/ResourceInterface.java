package cnr.ilc.rut.resource;

import java.util.Collection;

public interface ResourceInterface {
	Collection<String> getLanguages();
	Collection<Global> getGlobals();
	Collection<Concept> getConcepts();
	Collection<Word> getWords();
}

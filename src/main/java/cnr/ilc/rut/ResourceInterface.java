package cnr.ilc.rut;

import java.util.Collection;
import java.util.Map;

public interface ResourceInterface {
	Map<String, Object> getSummary();
	Map<String, String> getLexicons();
	Collection<Concept> getConcepts();
	Collection<Word> getWords();
}

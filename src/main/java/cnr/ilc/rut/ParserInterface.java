package cnr.ilc.rut;

import java.util.Collection;
import java.util.Map;

public interface ParserInterface {
	void parse() throws Exception;
	Map<String, Object> getMetadata();
	Map<String, String> getLexicons();
	Collection<Concept> getConcepts();
	Collection<Word> getWords();
	String getRdfType();
}

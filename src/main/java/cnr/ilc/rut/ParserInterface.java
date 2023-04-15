package cnr.ilc.rut;

import java.util.Map;

public interface ParserInterface {
	ResourceInterface parse() throws Exception;
	Map<String, Object> getMetadata();
}

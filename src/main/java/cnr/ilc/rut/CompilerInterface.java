package cnr.ilc.rut;

import java.util.Map;

public interface CompilerInterface {
	Map<String, Object> getMetadata();
	public String toSPARQL() throws Exception;
}

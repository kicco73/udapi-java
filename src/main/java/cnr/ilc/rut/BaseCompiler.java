package cnr.ilc.rut;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BaseCompiler implements CompilerInterface {
	protected Map<String,Object> metadata = new HashMap<>();
	protected InputStream inputStream;
	protected SPARQLWriter sparql;

	public BaseCompiler(InputStream inputStream, SPARQLWriter sparql) {
		this.inputStream = inputStream;
		this.sparql = sparql;
	}

	@Override
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	@Override
	public String toSPARQL() throws Exception {
		return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
	}
	
}

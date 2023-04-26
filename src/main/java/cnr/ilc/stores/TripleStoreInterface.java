package cnr.ilc.stores;

import java.util.Map;

import cnr.ilc.lemon.resource.ResourceInterface;

public interface TripleStoreInterface {
	public void store(ResourceInterface resource) throws Exception;
	public Map<String,Object> getMetadata() throws Exception;
	public String getSparql() throws Exception;
}

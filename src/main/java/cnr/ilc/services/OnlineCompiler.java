package cnr.ilc.services;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Map;

import org.json.simple.JSONObject;

import cnr.ilc.lemon.resource.ResourceInterface;
import cnr.ilc.rut.ParserInterface;
import cnr.ilc.stores.TripleStoreInterface;
import cnr.ilc.stores.filterstore.Filter;
import cnr.ilc.stores.filterstore.FilterStore;
import cnr.ilc.stores.filterstore.MetadataMerger;
import cnr.ilc.stores.filterstore.SqliteConnector;
import cnr.ilc.tbx.TbxParser;

public class OnlineCompiler {
	String resourceId;
	SqliteConnector db = new SqliteConnector();
	FilterStore filterStore = new FilterStore(db);
	MetadataMerger metadataMerger = new MetadataMerger(db);
	
	public OnlineCompiler(String resourceId) throws SQLException {
		this.resourceId = resourceId;
		String dbFile = "resources/"+resourceId+".db";// FIXME: HACK
		db.connect(dbFile);
	}

	public String parse(InputStream inputStream, String creator) throws Exception {
		TbxParser parser = new TbxParser(inputStream, creator);
		ResourceInterface resource = parser.parse();
		filterStore.store(resource);
		Map<String,Object> metadata = getMetadata();
		metadata.put("id", resourceId);
		return JSONObject.toJSONString(metadata);
	}

	public String getJson() throws Exception {
		return metadataMerger.getJson(new Filter());
	}

	public Map<String,Object> getMetadata() throws Exception {
		return metadataMerger.getMetadata(new Filter());
	}

	public String getJson(Filter filter) throws Exception {
		return metadataMerger.getJson(filter);
	}

	public ResourceInterface getResource(Filter filter) {
		filterStore.setFilter(filter);
		return filterStore;
	}

}

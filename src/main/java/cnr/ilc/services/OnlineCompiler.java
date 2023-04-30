package cnr.ilc.services;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Map;

import org.json.simple.JSONObject;

import cnr.ilc.lemon.resource.ResourceInterface;
import cnr.ilc.rut.Filter;
import cnr.ilc.sparql.SPARQLAssembler;
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

	public String analyse(InputStream inputStream) throws Exception {
		TbxParser parser = new TbxParser(inputStream);
		ResourceInterface resource = parser.parse();
		filterStore.store(resource);
		Map<String,Object> metadata = metadataMerger.getMetadata(new Filter());
		metadata.put("id", resourceId);
		return JSONObject.toJSONString(metadata);
	}

	public String filter(Filter filter) throws Exception {
		return metadataMerger.getJson(filter);
	}

	public String assemble(Filter filter, String namespace, String creator, int chunkSize) throws Exception {
		SPARQLAssembler assembler = new SPARQLAssembler(namespace, creator, chunkSize, filter);
		filterStore.setFilter(filter);
		assembler.serialise(filterStore);
		return assembler.getSparql();
	}

}

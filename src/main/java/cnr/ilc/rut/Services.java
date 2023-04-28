package cnr.ilc.rut;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import cnr.ilc.conllu.ConlluParser;
import cnr.ilc.lemon.resource.ResourceInterface;
import cnr.ilc.rut.utils.IdGenerator;
import cnr.ilc.rut.utils.Logger;
import cnr.ilc.sparql.SPARQLWriter;
import cnr.ilc.stores.filterstore.Filter;
import cnr.ilc.stores.filterstore.FilterStore;
import cnr.ilc.tbx.TbxParser;

public class Services {
	static public String outDir = "resources";
	static public String repository = "LexO";
	static public String graphURL = "http://localhost:7200";

	static public int chunkSize = 1500 * 1024;
	static private IdGenerator idGenerator = new IdGenerator();

	private static void deleteDir(File file) {
		File[] contents = file.listFiles();
		if (contents != null) {
			for (File f: contents) {
				deleteDir(f);
			}
		}
		file.delete();
	}

	private static void deleteResource(String id) {
		deleteDir(new File(outDir, id));
	}

    private static String getPathToResourceProperty(String id, String property) throws Exception {
		return new File(new File(outDir, id), property).getAbsolutePath();
	}

	private static void saveToResourceProperty(String id, String property, String content) throws Exception {
		String outName = getPathToResourceProperty(id, property);
		byte[] buffer = content.getBytes();
	
		new File(outDir, id).mkdirs();
		File targetFile = new File(outName);
		OutputStream outStream = new FileOutputStream(targetFile);
		outStream.write(buffer);
		outStream.close();
	}

	private static String loadFromResourceProperty(String id, String property) throws Exception {
		String inName = getPathToResourceProperty(id, property);
		InputStream inputStream = new FileInputStream(inName);
		String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		inputStream.close();
		return content;
	}

	private static FilterStore getStore(String resourceId, String namespace, String creator, Filter filter, boolean isNew) throws Exception {
		String dbFile = getPathToResourceProperty(resourceId, "sqlite.db");
		
		dbFile = "resources/"+resourceId+".db";// FIXME: HACK

		if (isNew) new File(dbFile).delete();
		return new FilterStore(namespace, creator, chunkSize, filter, dbFile);
	}


	static public String createResource(String input, String inputFileName, String fileType, String creator, String language, String namespace) throws Exception {
		ParserInterface parser = null;
		Map<String, Object> response = new HashMap<>();
		InputStream inputStream = new ByteArrayInputStream(input.getBytes());

		if (fileType.equals("tbx")) {
			parser = new TbxParser(inputStream, creator);
		} else if (fileType.equals("conllu")) {
			parser = new ConlluParser(inputStream, creator, language);
		}

		if (parser != null) {
			String basename = new File(inputFileName).getName();
			String resourceId = idGenerator.getId(basename);
			deleteResource(resourceId);
			saveToResourceProperty(resourceId, "input."+fileType, input);			

			ResourceInterface resource = parser.parse();
			FilterStore tripleStore = getStore(resourceId, namespace, creator, null, true);
			tripleStore.store(resource);
			response = tripleStore.getMetadata();
            response.put("id", resourceId);
		}
		return JSONObject.toJSONString(response);
	}

	static public String filterResource(String inputDir, String namespace, String creator, Filter filter) throws Exception {
		String resourceId = new File(inputDir).getName();
		FilterStore tripleStore = getStore(resourceId, namespace, creator, filter, false);
		tripleStore.setFilter(filter);
		String response = JSONObject.toJSONString(tripleStore.getMetadata());
		return response;
	}

	static public String assembleResource(String inputDir, String namespace, String creator, Filter filter) throws Exception {
		String resourceId = new File(inputDir).getName();
		FilterStore tripleStore = getStore(resourceId, namespace, creator, filter, false);
		tripleStore.setFilter(filter);
		String sparql = tripleStore.getSparql();
		saveToResourceProperty(resourceId, "sparql", sparql);
		return sparql;
	}

	static public String submitResource(String inputDir) throws Exception {
		String resourceId = new File(inputDir).getName();
		String statements = loadFromResourceProperty(resourceId, "sparql");
		GraphDBClient client = new GraphDBClient(graphURL, repository);

		client.postUpdate("CLEAR DEFAULT\n"); // FIXME: temporary hack

        String[] chunks = statements.split(SPARQLWriter.separator, 0);
        int n = 0;
        for (String chunk: chunks) {
            Logger.progress(++n * 100/chunks.length, "Submitting to GraphDB");
            client.postUpdate(chunk);
        }
		Logger.progress(100, "Done");
		return "";
	}

	static public String queryResource(String inputDir) throws Exception {

		String query = """
			PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>
			PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
			PREFIX lexinfo: <http://www.lexinfo.net/ontology/3.0/lexinfo#>
			PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
			PREFIX dct: <http://purl.org/dc/terms/>
			PREFIX dc: <http://purl.org/dc/elements/1.1/>
			SELECT ?subjectField ?conceptID 
			(GROUP_CONCAT(distinct(concat("DEFINITION @", lang(?def), ": ", str(?def), "\\nLINK: ",str(?identifier),"\\nSOURCE: ", str(?source), "\\n\\n"))) AS ?definitions)
			(GROUP_CONCAT(distinct(concat("CONTEXT @", lang(?context), ": ", str(?context), "\\nLINK: ",str(?identifierSense),"\\nSOURCE: ", str(?sourceSense), "\\n\\n"))) AS ?contexts)
			?wr ?pos ?termType ?normAuth ?note ?sourceExt ?seeAlso
			WHERE { ?concept a skos:Concept ;
					   skos:prefLabel ?conceptID .
					?sense ontolex:reference ?concept .
					?le ontolex:sense ?sense ;
						ontolex:canonicalForm [ ontolex:writtenRep ?wr ]
				OPTIONAL { ?concept skos:definition [ rdf:value ?def ;
													  dct:identifier ?identifier ;
													  dct:source ?source ] }
				OPTIONAL { ?sense ontolex:usage [ rdf:value ?context ;
													  dct:identifier ?identifierSense ;
													  dct:source ?sourceSense ] }
				OPTIONAL { ?concept skos:inScheme [ skos:prefLabel ?subjectField ] }
				OPTIONAL { ?le lexinfo:partOfSpeech ?_pos }
				OPTIONAL { ?le lexinfo:normativeAuthorization ?_normAuth }
				OPTIONAL { ?le lexinfo:termType ?_termType }
				OPTIONAL { ?le skos:note ?note }
				OPTIONAL { ?le dct:source ?sourceExt }
				OPTIONAL { ?le rdf:seeAlso ?seeAlso }
				BIND(strafter(str(?_pos), str(lexinfo:)) as ?pos)
				BIND(strafter(str(?_normAuth), str(lexinfo:)) as ?normAuth)
				BIND(strafter(str(?_termType), str(lexinfo:)) as ?termType)
			} 
			GROUP BY ?subjectField ?conceptID ?wr ?pos ?termType ?normAuth ?note ?sourceExt ?seeAlso
			ORDER BY ?subjectField		
		""";

		GraphDBClient client = new GraphDBClient(graphURL, repository);

		Logger.warn("Querying GraphDB");
		return client.postQuery(query); 
	}
}

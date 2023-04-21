package cnr.ilc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import cnr.ilc.conllu.ConlluParser;
import cnr.ilc.db.SqliteStore;
import cnr.ilc.rut.GraphDBClient;
import cnr.ilc.rut.IdGenerator;
import cnr.ilc.rut.ParserInterface;
import cnr.ilc.rut.ResourceInterface;
import cnr.ilc.sparql.SPARQLWriter;
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

	private static SqliteStore getStore(String resourceId, String namespace, String creator, boolean isNew) throws Exception {
		String dbFile = getPathToResourceProperty(resourceId, "sqlite.db");
		
		dbFile = "resources/"+resourceId+".db";// FIXME: HACK

		if (isNew) new File(dbFile).delete();
		return new SqliteStore(namespace, creator, chunkSize, dbFile);
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
			SqliteStore tripleStore = getStore(resourceId, namespace, creator, true);
			tripleStore.serialise(resource);
			
			response = parser.getMetadata();
            response.put("id", resourceId);
			//saveToResourceProperty(resourceId, "metadata.json", JSONObject.toJSONString(response));
		}

		return JSONObject.toJSONString(response);
	}

	static public String filterResource(String inputDir, String namespace, String creator, Collection<String> filterLanguages) throws Exception {
		String resourceId = new File(inputDir).getName();
		SqliteStore tripleStore = getStore(resourceId, namespace, creator, false);
		tripleStore.setLanguages(filterLanguages);
		String response = tripleStore.getMetadata();
		saveToResourceProperty(resourceId, "metadata.json", response);
		return response;
	}

	static public String assembleResource(String inputDir, String namespace, String creator) throws Exception {
		String resourceId = new File(inputDir).getName();
		SqliteStore tripleStore = getStore(resourceId, namespace, creator, false);
		String sparql = tripleStore.serialised();
		saveToResourceProperty(resourceId, "sparql", sparql);
		return sparql;
	}

	static public String submitResource(String inputDir) throws Exception {
		String resourceId = new File(inputDir).getName();
		String statements = loadFromResourceProperty(resourceId, "sparql");
		GraphDBClient client = new GraphDBClient(graphURL, repository);

		client.post("CLEAR DEFAULT\n"); // FIXME: temporary hack

        String[] chunks = statements.split(SPARQLWriter.separator, 0);
        int n = 0;
        for (String chunk: chunks) {
            System.err.print(String.format("\rPosting... %.0f%%", ++n * 100.0/chunks.length));
            client.post(chunk);
        }
        System.err.println();
		return "";
	}
}

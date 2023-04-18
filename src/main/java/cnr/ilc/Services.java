package cnr.ilc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import cnr.ilc.conllu.ConlluParser;
import cnr.ilc.db.SqliteStore;
import cnr.ilc.rut.IdGenerator;
import cnr.ilc.rut.ParserInterface;
import cnr.ilc.rut.ResourceInterface;
import cnr.ilc.sparql.TripleStoreInterface;
import cnr.ilc.tbx.TbxParser;

public class Services {
	static String outDir = "resources";
	static int chunkSize = 1500 * 1024;
	static IdGenerator idGenerator = new IdGenerator();

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
	
		deleteResource(id);
		new File(outDir, id).mkdirs();
		File targetFile = new File(outName);
		OutputStream outStream = new FileOutputStream(targetFile);
		outStream.write(buffer);
		outStream.close();
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
			String id = idGenerator.getId(basename);
			saveToResourceProperty(id, "input."+fileType, input);			

			ResourceInterface resource = parser.parse();
			Map<String,Object> metadata = parser.getMetadata();

			String dbFile = getPathToResourceProperty(id, "sqlite.db");
			
			// FIXME: HACK
			dbFile = "resources/"+id+".db";
			new File(dbFile).delete();

			TripleStoreInterface tripleStore = new SqliteStore(namespace, creator, chunkSize, dbFile);
			tripleStore.serialise(resource);
			
            response.put("id", id);
            response.put("metadata", metadata);
			saveToResourceProperty(id, "metadata.json", JSONObject.toJSONString(response));
		}

		return JSONObject.toJSONString(response);
	}
}

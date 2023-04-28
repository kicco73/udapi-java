/**
 * @author Enrico Carniani
 */

package cnr.ilc;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONValue;

import cnr.ilc.conllu.ConlluParser;
import cnr.ilc.lemon.resource.ResourceInterface;
import cnr.ilc.rut.ParserInterface;
import cnr.ilc.rut.Services;
import cnr.ilc.rut.utils.DateProvider;
import cnr.ilc.rut.utils.IdGenerator;
import cnr.ilc.rut.utils.Logger;
import cnr.ilc.sparql.SPARQLWriter;
import cnr.ilc.stores.MemoryStore;
import cnr.ilc.stores.TripleStoreInterface;
import cnr.ilc.stores.filterstore.Filter;
import cnr.ilc.stores.filterstore.FilterStore;
import cnr.ilc.rut.GraphDBClient;
import cnr.ilc.tbx.TbxParser;

public class Main {
    boolean isConnlu = false;
    boolean isTbx = false;
    boolean isDb = false;
    boolean isSparql = false;
    boolean isJson = false;
    String service = null;
    String language = "it";
    String creator = "bot";
    int chunkSize = 1500 * 1024;
    String namespace = "http://txt2rdf/test#";
    String exportConll = null;
    String outSparql = null;
    String[] fileNames = new String[0];
    String format = null;
    Filter filter = new Filter();

    public static void main(String[] args) throws Exception {
        new Main()
            .parse(args)
            .run();
    }

    private Main parse(String[] args) throws ParseException {
        int startIndex = 0;
        while (startIndex < args.length) {
            switch (args[startIndex++]) {
                case "-d":
                case "--datetime":
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX");      
                    Date date = formatter.parse(args[startIndex++]);      
                    DateProvider.getInstance(date);
                    break;
                case "-a":
                case "--creator":
                    creator = args[startIndex++];     
                    break;   
                case "-g":
                case "--graphdb-url":
                    Services.graphURL = args[startIndex++];
                    break;
                case "-i":
                case "--input-format":
                    format = args[startIndex++];
                    if (format.equals("conllu")) 
                        isConnlu = true;
                    else if (format.equals("tbx")) 
                        isTbx = true;
                    else if (format.equals("sparql")) 
                        isSparql = true;
                    else if (format.equals("sqlite")) 
                        isDb = true;
                    else
                        throw new IllegalArgumentException(String.format("Unknown format %s: must be one of conllu, tbx, sparql, sqlite", format));
                    break;
                case "--filter-languages":
                    if (args[startIndex++].length() > 0)
                        filter.setLanguages(Arrays.asList(args[startIndex-1].split(",")));
                    break;
                case "--filter-dates":
                    if (args[startIndex++].length() > 0)
                        filter.setDates(Arrays.asList(args[startIndex-1].split(",")));
                    break;
                case "--filter-subjectfields":
                    if (args[startIndex++].length() > 0)
                        filter.setSubjectFields(Arrays.asList(args[startIndex-1].split(",")));
                    break;
                case "--filter-no-concepts":
                    filter.setNoConcepts(true); 
                    break;
                    case "--filter-no-senses":
                    filter.setNoSenses(true); 
                    break;
                case "--filter-translate-terms":
                    filter.setTranslateTerms(true); 
                    break;
                case "--filter-translate-senses":
                    filter.setTranslateSenses(true); 
                    break;
                case "--filter-synonyms":
                    filter.setSynonyms(true); 
                    break;
                case "-j":
                case "--json":
                    isJson = true;
                    break;
                case "-r":
                case "--repository":
                    Services.repository = args[startIndex++];
                    break;
                case "-S":
                case "--chunk-size":
                    chunkSize = Integer.parseInt(args[startIndex++]);        
                    break;
                case "-l":
                case "--language":
                    language = args[startIndex++];
                    break;
                case "-x":
                case "--export-conll":
                    exportConll = args[startIndex++];
                    break;
                case "-n":
                case "--namespace":
                    namespace = args[startIndex++];
                    break;
                case "-O":
                case "--output-dir":
                    Services.outDir = args[startIndex++];
                    break;
                case "-s":
                case "--service":
                    service = args[startIndex++];
                    break;
                case "--":
                    fileNames = Arrays.copyOfRange(args, startIndex, args.length);
                    startIndex = args.length;
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Unknown option: %s", args[startIndex-1]));
            }
        }
        if ((service == null || service.equals("analyse")) && (isTbx? 1 : 0) + (isConnlu? 1 : 0) + (isSparql? 1 : 0) + (isDb? 1 : 0) != 1) 
            throw new IllegalArgumentException("--input-format option must be specified once (and once only).");

        return this;
    }

    private void run() throws Exception {
        if (fileNames.length > 0)
            for(String fileName: fileNames) {
                Logger.log(String.format("\nProcessing: %s", fileName));
                if (service != null) runService(fileName);
                else processFile(fileName);
            }
        else if (service != null) runService(null);
        else processFile(null);
    }

    private ParserInterface makeParser(InputStream inputStream) throws Exception {
        if (isTbx) {
            return new TbxParser(inputStream, creator);
        } else if (isConnlu) {
            return new ConlluParser(inputStream, creator, language);
        }
        return null;
    }

    private void runService(String fileName) throws Exception {
        String response = "";

        switch(service) {
            case "analyse":
                String input = new String(System.in.readAllBytes());
                response = Services.createResource(input, fileName == null? "stdin" : fileName, format, creator, language, namespace);
                break;
            case "filter":
                response = Services.filterResource(fileName, namespace, creator, filter);
                break;
            case "assemble":
                response = Services.assembleResource(fileName, namespace, creator, filter);
                break;
            case "submit":
                response = Services.submitResource(fileName);
                break;
            case "query":
                response = Services.queryResource(fileName);
                break;
        default:
                throw new IllegalArgumentException(String.format("Unknown service: %s", service));
        }

        System.out.println(response);
    }

    private void processFile(String fileName) throws Exception {
        InputStream inputStream = fileName == null? System.in : new FileInputStream(fileName);
        Map<String, Object> metadata = null;
        String statements = null;

        if (isSparql) {
            statements = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } else {
            TripleStoreInterface tripleStore;

            if (isDb) {
                tripleStore = new FilterStore(namespace, creator, chunkSize, fileName);
            } else {
                tripleStore = new MemoryStore(namespace, creator, chunkSize);
                ParserInterface parser = makeParser(inputStream);
                ResourceInterface resource = parser.parse();
                tripleStore.store(resource);
                metadata = tripleStore.getMetadata();
    
                if (isConnlu && exportConll != null && parser instanceof ConlluParser) {
                    ((ConlluParser)parser).writeConll(exportConll);
                }  
            }
            statements = tripleStore.getSparql();
        }
        
        String output = statements;

        if (isJson) {
            Map<String, Object> response = new HashMap<>();
            IdGenerator idGenerator = new IdGenerator();
            response.put("metadata", metadata);
            response.put("sparql", statements);
            response.put("id", idGenerator.getId(fileName == null? "stdin" : fileName));
            output = JSONValue.toJSONString(response); 
        }

        if (Services.outDir == null || fileName == null) {
            if (!isSparql) System.out.println(output);
        } else if (statements != null) {
            saveStatementsUsingInFileName(fileName, ".sparql", statements);            
        }

        if (Services.graphURL != null && statements != null) {
            uploadStatements(Services.graphURL, Services.repository, statements);
        }        
    }

    private static void saveStatementsUsingInFileName(String inFile, String extension, String statements) throws Exception {
        String pathName = getOutFileNameUsingInFileName(inFile, Services.outDir, extension);
            PrintWriter writer = new PrintWriter(pathName, "UTF-8");
        writer.println(statements);
        writer.close();
    }

    private static String getOutFileNameUsingInFileName(String inFile, String outDir, String extension) throws Exception {
        String fileName = new File(inFile).getName();
        int endIndex = fileName.lastIndexOf(".");
        fileName = fileName.substring(0, endIndex)+extension; 
        String pathName = new File(outDir, fileName).getAbsolutePath();
        return pathName;
    }

    private static void uploadStatements(String graphURL, String repository, String statements) throws Exception {
        GraphDBClient client = new GraphDBClient(graphURL, repository);
        String[] chunks = statements.split(SPARQLWriter.separator, 0);
        int n = 0;
        for (String chunk: chunks) {
            System.err.print(String.format("\rPosting... %.0f%%", ++n * 100.0/chunks.length));
            client.postUpdate(chunk);
        }
        System.err.println();
    }
    
}

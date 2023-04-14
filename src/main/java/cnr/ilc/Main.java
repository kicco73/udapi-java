/**
 * @author Enrico Carniani
 */

package cnr.ilc;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONValue;

import cnr.ilc.conllu.ConnluCompiler;
import cnr.ilc.rut.BaseCompiler;
import cnr.ilc.rut.CompilerInterface;
import cnr.ilc.rut.DateProvider;
import cnr.ilc.rut.GraphDBClient;
import cnr.ilc.rut.IdGenerator;
import cnr.ilc.rut.SPARQLWriter;
import cnr.ilc.tbx.TbxCompiler;

public class Main {
    boolean isSparql = false;
    boolean isConnlu = false;
    boolean isTbx = false;
    boolean isJson = false;
    String graphURL = null;
    String repository = "LexO";
    String language = "it";
    String creator = "bot";
    int chunkSize = 15000;
    String namespace = "http://txt2rdf/test#";
    String exportConll = null;
    String outSparql = null;
    String outDir = null;
    String[] fileNames = new String[0];

    public static void main(String[] args) throws Exception {
        new Main()
            .parse(args)
            .run();
    }

    private Main parse(String[] args) throws ParseException {
        int startIndex = 0;
        while (startIndex < args.length) {
            switch (args[startIndex++]) {
                case "-c":
                case "--conllu":
                    isConnlu = true;
                    break;
                case "-t":
                case "--tbx":
                    isTbx = true;
                    break;
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
                    graphURL = args[startIndex++];
                    break;
                case "-j":
                case "--json":
                    isJson = true;
                    break;
                case "-r":
                case "--repository":
                    repository = args[startIndex++];
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
                case "-o":
                case "--output-dir":
                    outDir = args[startIndex++];
                    break;
                case "-s":
                case "--sparql":
                    isSparql = true;
                    break;
                case "--":
                    fileNames = Arrays.copyOfRange(args, startIndex, args.length);
                    startIndex = args.length;
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Unknown option: %s", args[startIndex-1]));
            }
        }
        if ((isTbx? 1 : 0) + (isConnlu? 1 : 0) + (isSparql? 1 : 0) != 1) 
            throw new IllegalArgumentException("One (and one only) of the --tbx, --conllu, --sparql switches must be set.");

        return this;
    }

    private void run() throws Exception {
        if (fileNames.length > 0)
            for(String fileName: fileNames) {
                System.err.println(String.format("\nCompiling: %s", fileName));
                processFile(fileName);    
            }
        else {
            processFile(null);
        }
    }

    private void processFile(String fileName) throws Exception {
        String statements = null;
        SPARQLWriter sparql = new SPARQLWriter(namespace, creator, chunkSize);
        Map<String, Object> metadata = null;
        InputStream inputStream = fileName == null? System.in : new FileInputStream(fileName);
        CompilerInterface compiler = null;
        
        if (isConnlu) {
            compiler = new ConnluCompiler(inputStream, sparql, language);
        } else if (isTbx) {
            compiler = new TbxCompiler(inputStream, sparql);
        } else if (isSparql) {
            compiler = new BaseCompiler(inputStream, sparql);
        }

        statements = compiler.toSPARQL();
        metadata = compiler.getMetadata();

        String output = statements;

        if (isJson) {
            Map<String, Object> response = new HashMap<>();
            IdGenerator idGenerator = new IdGenerator();
            response.put("metadata", metadata);
            response.put("sparql", statements);
            response.put("id", idGenerator.getId(fileName == null? "stdin" : fileName));
            output = JSONValue.toJSONString(response); 
        }

        if (exportConll != null && compiler instanceof ConnluCompiler) {
            ((ConnluCompiler)compiler).writeConll(exportConll);
        }  

        if (graphURL != null) {
            uploadStatements(graphURL, repository, statements);
        }
        
        if (outDir == null || fileName == null) {
            if (!isSparql) System.out.println(output);
        } else {
            saveStatementsUsingInFileName(fileName, outDir, statements);            
        }
    }

    private static void saveStatementsUsingInFileName(String inFile, String outDir, String statements) throws Exception {
        String fileName = new File(inFile).getName();
        int endIndex = fileName.lastIndexOf(".");
        fileName = fileName.substring(0, endIndex)+".sparql"; 
        String pathName = new File(outDir, fileName).getAbsolutePath();
    
        PrintWriter writer = new PrintWriter(pathName, "UTF-8");
        writer.println(statements);
        writer.close();
    }

    private static void uploadStatements(String graphURL, String repository, String statements) throws Exception {
        GraphDBClient client = new GraphDBClient(graphURL, repository);
        String[] chunks = statements.split(SPARQLWriter.separator, 0);
        int n = 0;
        for (String chunk: chunks) {
            System.err.print(String.format("\rPosting... %.0f%%", ++n * 100.0/chunks.length));
            client.post(chunk);
        }
        System.err.println();
    }
    
}

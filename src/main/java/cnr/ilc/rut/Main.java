/**
 * @author Enrico Carniani
 */

package cnr.ilc.rut;

import java.io.File;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import cnr.ilc.conllu.Connlu2Sparql;
import cnr.ilc.tbx.Tbx2Sparql;

public class Main {
    boolean isConnlu = false;
    boolean isTbx = false;
    String graphURL = null;
    String repository = "LexO";
    String language = "it";
    String creator = "bot";
    int chunkSize = 15000;
    String namespace = "http://txt2rdf/test#";
    String exportConll = null;
    String outSparql = null;
    String outDir = null;
    String[] fileNames = null;

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
                case "-r":
                case "--repository":
                    repository = args[startIndex++];
                    break;
                case "-s":
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
                case "--":
                    fileNames = Arrays.copyOfRange(args, startIndex, args.length);
                    startIndex = args.length;
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Unknown option: %s", args[startIndex-1]));
            }
        }
        if (!(isConnlu ^ isTbx))
            throw new IllegalArgumentException("Either --tbx or --conllu switch must be set.");
        if (fileNames == null)
           throw new IllegalArgumentException("End of options delimiter (--) must be present.");
        return this;
    }

    private void run() throws Exception {
        for(String fileName: fileNames) {
            System.err.println(String.format("\nCompiling: %s", fileName));
            processFile(fileName);
        }
    }

    private void processFile(String inputFileName) throws Exception {
        String statements = null;
        SPARQLWriter sparql = new SPARQLWriter(namespace, creator, chunkSize);

        if (isConnlu) {
            Connlu2Sparql sparqlConverter = new Connlu2Sparql(inputFileName, sparql, language);
            statements = sparqlConverter.createSPARQL();

            if (exportConll != null) {
                sparqlConverter.writeConll(exportConll);
            }    
        }

        if (isTbx) {
            Tbx2Sparql sparqlConverter = new Tbx2Sparql(inputFileName, sparql);
            statements = sparqlConverter.createSPARQL();
            System.err.println(String.format("File size: %d", sparqlConverter.fileSize));
            System.err.println(String.format("TBX dialect: %s", sparqlConverter.tbxType));
            System.err.println(String.format("Number of concepts: %d", sparqlConverter.getNumberOfConcepts()));
            System.err.println(String.format("Number of languages: %d", sparqlConverter.getNumberOfLanguages()));
            System.err.println(String.format("Number of terms: %d", sparqlConverter.getNumberOfTerms()));
        }

        saveStatementsUsingInFileName(inputFileName, outDir, statements);

        if (graphURL != null) {
            uploadStatements(graphURL, repository, statements);
        } else if (outDir == null) {
            System.out.println(statements);
        }
    }

    private static void saveStatementsUsingInFileName(String inFile, String outDir, String statements) throws Exception {
        if (outDir == null) return;

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

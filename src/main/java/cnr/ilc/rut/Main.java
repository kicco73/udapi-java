/**
 * @author Enrico Carniani
 */

package cnr.ilc.rut;

import cnr.ilc.conllu.Connlu2Sparql;
import cnr.ilc.tbx.Tbx2Sparql;

public class Main {
    String inCoNLL = null;
    String inTbx = null;
    String graphURL = null;
    String repository = "LexO";
    String language = "it";
    String creator = "bot";
    int chunkSize = 1250;
    String namespace = "http://txt2rdf/test#";
    String exportConll = null;

    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    private void run(String[] args) throws Exception {
        int startIndex = 0; 
        while (startIndex < args.length) {
            switch (args[startIndex++]) {
                case "-i":
                case "--input-conll":
                    inCoNLL = args[startIndex++];
                    break;
                case "-t":
                case "--input-tbx":
                    inTbx = args[startIndex++];
                    break;
                case "-c":
                case "--creator":
                    creator = args[startIndex++];     
                    break;   
                case "-o":
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
                default:
                    System.err.println(String.format("Unknown option: %s", args[startIndex-1]));
                    break;
            }
        }
        String statements = null;
        SPARQLWriter sparql = new SPARQLWriter(namespace, creator, chunkSize);

        if (inCoNLL != null) {
            Connlu2Sparql sparqlConverter = new Connlu2Sparql(inCoNLL, sparql);
            statements = sparqlConverter.createSPARQL();
    
            if (exportConll != null) {
                sparqlConverter.writeConll(exportConll);
            }    
        }

        if (inTbx != null) {
            Tbx2Sparql sparqlConverter = new Tbx2Sparql(inTbx, sparql);
            statements = sparqlConverter.createSPARQL();
            System.err.println(String.format("File size: %d", sparqlConverter.fileSize));
            System.err.println(String.format("TBX dialect: %s", sparqlConverter.tbxType));
            System.err.println(String.format("Number of concepts: %d", sparqlConverter.getNumberOfConcepts()));
            System.err.println(String.format("Number of languages: %d", sparqlConverter.getNumberOfLanguages()));
            System.err.println(String.format("Number of terms: %d", sparqlConverter.getNumberOfTerms()));
        }

        if (graphURL != null) {
            uploadStatements(graphURL, repository, statements);
        } else {
            System.out.println(statements);
        }
    }

    private static void uploadStatements(String graphURL, String repository, String statements) throws Exception {
        GraphDBClient client = new GraphDBClient(graphURL, repository);
        String[] chunks = statements.split("# \\[data-chunk\\]\n", 0);
        int n = 0;
        for (String chunk: chunks) {
            System.out.print(String.format("\rPosting... %.0f%%", ++n * 100.0/chunks.length));
            client.post(chunk);
        }
        System.out.println();
    }
    
}

package cnr.ilc.conllu.core.io;

import java.io.BufferedReader;
import java.util.Optional;

import cnr.ilc.conllu.core.Document;
import cnr.ilc.conllu.core.Sentence;

/**
 * Used for reading document from different formats.
 *
 * @author Martin Vojtek
 */
public interface DocumentReader {
    /**
     * Reads bundles into document.
     *
     * @return Document document containing some bundles
     * @throws UdapiIOException If any error occurs
     */
    Document readDocument() throws UdapiIOException;

    /**
     * Reads bundles into Document.
     *
     * @param document document to read into
     * @throws UdapiIOException If any error occurs
     */
    void readInDocument(Document document) throws UdapiIOException;

    /**
     * Reads tree into document.
     *
     * @param document document to read into
     * @return tree of the sentence
     * @throws UdapiIOException If any error occurs
     */
    Optional<Sentence> readTree(final Document document) throws UdapiIOException;

    /**
     * Reads tree into document with given reader.
     *
     * @param bufferedReader reader encapsulating input to process
     * @param document document to read into
     * @return tree of the sentence
     * @throws UdapiIOException If any error occurs
     */
    Optional<Sentence> readTree(BufferedReader bufferedReader, final Document document) throws UdapiIOException;
}

package cnr.ilc.conllu.core.io;

import java.io.Writer;
import java.nio.file.Path;

import cnr.ilc.conllu.core.Document;

/**
 * Used to write documents into different formats.
 *
 * @author Martin Vojtek
 */
public interface DocumentWriter {
    /**
     * Writes document to given writer.
     *
     * Writer is closed after processing.
     *
     * @param document document to write
     * @param writer writer to use
     * @throws UdapiIOException If any error occurs
     */
    void writeDocument(Document document, Writer writer) throws UdapiIOException;

    /**
     * Writes document to given output path.
     *
     * @param document document to write
     * @param outPath path where the document will be serialized
     * @throws UdapiIOException If any error occurs
     */
    void writeDocument(Document document, Path outPath) throws UdapiIOException;

}

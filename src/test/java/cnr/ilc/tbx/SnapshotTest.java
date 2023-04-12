package cnr.ilc.tbx;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import cnr.ilc.rut.Main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class SnapshotTest {

    static Path tmp;
    final static String tmpName = "output";

    @BeforeClass
    public static void setUpOnce() throws IOException {
        tmp = Files.createTempDirectory("rut");
    }

    @AfterClass
    public static void tearDownOnce() throws IOException {
        Files.delete(tmp);
    }

    private Set<String> listFiles(String dir) {
        return Stream.of(new File(dir).listFiles())
          .filter(file -> !file.isDirectory() && !file.isHidden())
          .map(File::getName)
          .collect(Collectors.toSet());
    }

    private String replaceExtension(String name, String newExtension) {
        int endIndex = name.lastIndexOf(".");
        name = name.substring(0, endIndex)+ "." + newExtension; 
        return name;
    }
    
    private String exec(String args, String fileName) throws Exception {
        args = String.format("--output-dir %s %s -- %s", tmp.toString(), args, fileName);
        Main.main(args.split(" "));
        Path path = new File(fileName).toPath();
        String name = path.getFileName().toString();
        name = replaceExtension(name, "sparql"); 
        Path tmpPath = tmp.resolve(name);
        String output = Files.readString(tmpPath);
        tmpPath.toFile().delete();
        return output;
    }

    private void assertIsSameSnapshot(String fileName, String actual) throws IOException {
        Path file = new File(fileName).toPath();
        String expected = Files.readString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testGivenTbxFilesThenOutputMatchesSnapshots() throws Exception {
        String inFileDir =  Paths.get(getClass().getResource("input").toURI()).toString();
        String snapshotDir =  Paths.get(getClass().getResource("sparql").toURI()).toString();

        for(String inFileName: listFiles(inFileDir)) {
            String inPath = new File(inFileDir, inFileName).getAbsolutePath();
            System.out.println(inPath);
            String output = exec("--tbx --creator kicco --datetime 2023-04-10T10:02+02:00", inPath);
            String snapshotFileName = replaceExtension(inFileName, "sparql");
            snapshotFileName = new File(snapshotDir, snapshotFileName).getAbsolutePath();
            assertIsSameSnapshot(snapshotFileName, output);
        }
    }
}

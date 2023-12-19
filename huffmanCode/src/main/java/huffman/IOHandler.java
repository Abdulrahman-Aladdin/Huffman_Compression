package huffman;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

public class IOHandler {

    private final String path;
    private final int chunkSize;

    IOHandler (String path, int chunkSize) {
        this.path = path;
        this.chunkSize = chunkSize;
    }
    Logger logger = Logger.getLogger(IOHandler.class.getName());
    public List<String> readNormalFile() {
        String bytes = "";
        try {
            bytes = Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(path)));

        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        return toChunks(bytes);
    }

    private List<String> toChunks(String bytes) {
        List<String> chunks = new ArrayList<>();
        int i = 0;
        while (i < bytes.length()) {
            chunks.add(bytes.substring(i, Math.min(i + chunkSize, bytes.length())));
            i += chunkSize;
        }
        return chunks;
    }

    private String getOutputPath() {
        Path inputPath = Path.of(path);
        String fileName = inputPath.getFileName().toString();
        return inputPath.resolveSibling("20010824." + chunkSize + "." + fileName + ".hc").toString();
    }

    public void writeBytes(String data) {
        try {
            Files.write(Paths.get(getOutputPath()), Base64.getDecoder().decode(data));

        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
    }

    public byte[] readCompressed() {
        try {
            return Files.readAllBytes(Paths.get(path));

        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        return null;
    }

    public void writeCompressed(byte[] encoded) {
        try {
            Files.write(Paths.get(getOutputPath()), encoded);

        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
    }

}

package huffman;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
    Name: Abdulrahman Aladdin - ID: 20010824

    “I acknowledge that I am aware of the academic integrity guidelines of this
    course, and that I worked on this assignment independently without any
    unauthorized help.”
*/

public class Main {
    public static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        if (args.length < 2) {
            logger.severe("Usage: java -jar huffman_20010824.jar <c/d> <path> <optional: n>");
        }

        char mode = args[0].charAt(0);
        String path = args[1];

        if (mode == 'c') {    // compression
            int n = args[2] == null ? 4 : Integer.parseInt(args[2]); // default chunk size is 4 [bytes]

            IOHandler ioHandler = new IOHandler(path, n);
            List<String> bytes = ioHandler.readNormalFile();

            HuffmanCompressor compressor = new HuffmanCompressor();
            String compressed = compressor.compress(bytes);

            logger.info(compressed);
        } else {    // decompression
            logger.log(Level.INFO, "Mode: {0}, Path: {1}", new Object[]{mode, path});
        }
    }
}
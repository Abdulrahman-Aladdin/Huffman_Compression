package huffman;

import java.util.logging.Logger;

/*
    Name: Abdulrahman Aladdin - ID: 20010824

    "I acknowledge that I am aware of the academic integrity guidelines of this
    course, and that I worked on this assignment independently without any
    unauthorized help."
*/

public class Main {

    static Logger logger = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) {

        if (args.length < 2) {
            logger.severe("USAGE: java -jar huffman_20010824.jar <c / d> <absolute path> <in case of compressing: chunk size>");
        }

        char mode = args[0].charAt(0);
        String path = args[1];

        if (mode == 'c') {
            int chunkSize = Integer.parseInt(args[2]);
            Compressor compressor = new Compressor();
            compressor.compress(path, chunkSize);
        } else {
            Decompressor decompressor = new Decompressor(path);
            decompressor.decompress();
        }
    }
}

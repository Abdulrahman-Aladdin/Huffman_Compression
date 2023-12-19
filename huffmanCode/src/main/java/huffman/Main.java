package huffman;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
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

//        if (args.length < 2) {
//            logger.severe("Usage: java -jar huffman_20010824.jar <c/d> <path> <optional: n>");
//        }
//
//        char mode = args[0].charAt(0);
//        String path = args[1];
//
//        if (mode == 'c') {    // compression
//            int n = args[2] == null ? 4 : Integer.parseInt(args[2]); // default chunk size is 4 [bytes]
//            HuffmanCompressor compressor = new HuffmanCompressor(new IOHandler(path, n));
//            compressor.compress();
//
//        } else {    // decompression
//            logger.log(Level.INFO, "Mode: {0}, Path: {1}", new Object[]{mode, path});
//        }
        String path = "/media/ebn_aladdin/NoName/My-GitHub/Huffman_Compression/huffmanCode/gbbct10.seq";
        int n = 1;
        int bufferSize = 1024;

        try (FileChannel fileChannel = FileChannel.open(Paths.get(path), StandardOpenOption.READ)) {
            long size = fileChannel.size();
            long position = 0;
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            while (position < size) {
                long remaining = size - position;
                long chunk = remaining > bufferSize ? bufferSize : remaining;
                fileChannel.read(buffer);
                buffer.flip();
                process(buffer);
                buffer.clear();
                position += chunk;
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }

//        HuffmanCompressor compressor = new HuffmanCompressor(new IOHandler(path, n));
//        compressor.compress();
    }

    public static void process(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String s = Base64.getEncoder().encodeToString(bytes);

        List<String> chunks = toChunks(s, 1);

        
    }

    public static List<String> toChunks(String bytes, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int i = 0;
        while (i < bytes.length()) {
            chunks.add(bytes.substring(i, Math.min(i + chunkSize, bytes.length())));
            i += chunkSize;
        }
        return chunks;
    }
}
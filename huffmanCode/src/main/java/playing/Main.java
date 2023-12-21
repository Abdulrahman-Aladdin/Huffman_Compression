package playing;

public class Main {

    public static void main(String[] args) {
        int chunkSize = 1;
        Compressor compressor = new Compressor();
        String fileName = "dd.seq";
        String path = "/media/ebn_aladdin/NoName/My-GitHub/Huffman_Compression/huffmanCode/" + fileName;
        String outputPath = "/media/ebn_aladdin/NoName/My-GitHub/Huffman_Compression/huffmanCode/20010824." + chunkSize + "." + fileName + ".hc";

       compressor.compress(path, chunkSize);

        Decompressor decompressor = new Decompressor(outputPath);
        decompressor.decompress();
    }
}

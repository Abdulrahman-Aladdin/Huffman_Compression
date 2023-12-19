package playing;

public class Main {

    public static void main(String[] args) {
        String path = "/media/ebn_aladdin/NoName/My-GitHub/Huffman_Compression/huffmanCode/luca.mp4";
        String outputPath = "/media/ebn_aladdin/NoName/My-GitHub/Huffman_Compression/huffmanCode/20010824.1.luca.mp4.hc";
        int chunkSize = 1;
        Compressor compressor = new Compressor();
        compressor.compress(path, chunkSize);

        Decompressor decompressor = new Decompressor(outputPath);
        decompressor.decompress();
    }
}

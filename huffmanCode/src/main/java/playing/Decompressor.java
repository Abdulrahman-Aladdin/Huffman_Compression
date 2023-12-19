package playing;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.util.HashMap;

public class Decompressor {
    private final String path;
    private final int bufferSize = 1024 * 1024 * 32;
    private int offset;
    HashMap<String, String> codes;

    public Decompressor(String path) {
        this.path = path;
        codes = new HashMap<>();
        offset = 0;
    }

    public void decompress() {
        System.out.println("Decompressing...");
        long startTime = System.currentTimeMillis();
        readFile();
        long end = System.currentTimeMillis();
        System.out.println("time: " + (end - startTime) / 1000.0);
    }

    private void readFile() {
        try (FileInputStream fis = new FileInputStream(path)) {
            offset = fis.read();
            readMap(fis);
            readCompressedData(fis);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void readCompressedData(FileInputStream fis) throws IOException {
        long totalLength = fis.available();
        FileOutputStream fos = new FileOutputStream(getOutputPath(path), true);
        String binaryString = "";
        while (totalLength > 0) {
            byte[] buffer = new byte[Math.min(bufferSize, (int) totalLength)];
            int read = fis.read(buffer);
            StringBuilder sb = new StringBuilder();
            for (byte b : buffer) {
                sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
                if (totalLength == 1) {
                    sb.delete(sb.length() - offset, sb.length());
                }
                totalLength--;
            }
            String[] data = getOriginalData(sb.toString(), binaryString);
            binaryString = data[1];
            fos.write(StringToByteArray(data[0]));
        }
    }

    private byte[] StringToByteArray(String s) {
        byte[] bytes = new byte[s.length()];
        for (int i = 0; i < s.length(); i++) {
            bytes[i] = (byte) s.charAt(i);
        }
        return bytes;
    }

    private String[] getOriginalData(String binaryString, String remainingString) {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder(remainingString);
        for (int i = 0; i < binaryString.length(); i++) {
            temp.append(binaryString.charAt(i));
            if (codes.containsKey(temp.toString())) {
                sb.append(codes.get(temp.toString()));
                temp = new StringBuilder();
            }
        }
        return new String[] { sb.toString(), temp.toString() };
    }

    private String getOutputPath(String path) {
        Path inputPath = Path.of(path);
        String fileName = inputPath.getFileName().toString();
        String[] parts = fileName.split("\\.");
        if (parts.length == 5) {
            return inputPath.resolveSibling("extracted." + parts[2] + "." + parts[3]).toString();
        } else {
            return inputPath.resolveSibling("extracted." + parts[2]).toString();
        }
    }

    private void readMap(FileInputStream fis) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(fis);
        codes = (HashMap<String, String>) ois.readObject();
    }
}

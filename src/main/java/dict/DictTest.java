package dict;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Random;

public class DictTest {

    @Test
    public void write() throws IOException {
        String[] dict = new String[5 * 1000 * 1000];
        for (int i = 0; i < dict.length; i++) {
            dict[i] = gen();
        }
        writeDict(dict);
    }

    @Test
    public void read() throws FileNotFoundException {
        File file = new File("dict");

        try (RandomAccessFile r = new RandomAccessFile(file, "r")) {
            int len = r.readInt();

            int[] pos = new int[len];
            for (int i = 0; i < pos.length; i++) {
                pos[i] = r.readInt();
            }

            long t1 = System.currentTimeMillis();
            for (int i = 0; i < 1000000; i++) {
                get(r, pos, new Random().nextInt(5 * 1000 * 1000) + 1);
            }
            System.out.println("duration:" + (System.currentTimeMillis() - t1));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void printOri(String[] dict) {
        int total = 0;
        for (int i = 0; i < 100; i++) {
            int currentLen = dict[i].getBytes(Charset.forName("UTF-8")).length;
            System.out.println("[" + i + "]:[" + (total += currentLen) + "]" + currentLen);
            System.out.println(dict[i]);
        }
    }

    private static void writeDict(String[] dict) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DictHeader header = new DictHeader(dict);
        DictValue value = new DictValue(dict);
        DictHeader.DictHeaderSerializer.DICT_HEADER_SERIALIZER.serialize(header, new DataOutputStream(out));
        DictValue.DictValueSerializer.DICT_VALUE_SERIALIZER.serialize(value, new DataOutputStream(out));

        try (OutputStream outputStream = new FileOutputStream("dict")) {
            out.writeTo(outputStream);
            out.close();
        }
    }

    public static String get(RandomAccessFile in, int[] pos, int n) throws IOException {
        int p = pos[n - 1];
        int l = pos[n] - p;
        byte[] r = new byte[l];
        in.seek(p);
        in.read(r);
        return new String(r, Charset.forName("UTF-8"));
    }

    public static String gen() {
        return RandomStringUtils.randomAlphanumeric(new Random().nextInt(100) + 1);
    }
}
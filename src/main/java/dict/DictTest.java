package dict;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Random;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

public class DictTest {
    private int CAP = 1000;

    @Test
    public void write() throws IOException {
        String[] dict = new String[CAP];
        for (int i = 0; i < dict.length; i++) {
            dict[i] = gen();
        }
        writeDict(dict);
    }

    @Test
    public void read() throws IOException {
        write();
        File file = new File("dict");

        try (RandomAccessFile r = new RandomAccessFile(file, "r")) {
            int len = r.readInt();

            int[] pos = new int[len];
            for (int i = 0; i < pos.length; i++) {
                pos[i] = r.readInt();
            }

            long t1 = System.currentTimeMillis();
            for (int i = 0; i < 1000000; i++) {
                get(r, pos, new Random().nextInt(CAP));
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
        byte[] r;
        if (n == 0) {
            r = new byte[pos[0]];
            in.read(r);
        } else {
            int p = pos[n - 1];
            int l = pos[n] - p;
            r = new byte[l];
            in.seek(p);
            in.read(r);

        }
        return new String(r, Charset.forName("UTF-8"));
    }

    public static String gen() {
        return RandomStringUtils.randomAlphanumeric(new Random().nextInt(100) + 1);
    }
}
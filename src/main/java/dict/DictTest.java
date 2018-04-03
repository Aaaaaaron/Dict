package dict;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.RandomStringUtils;

public class DictTest {
    public static void main(String[] args) throws IOException {
        String[] dict = new String[1000 * 1000];
        for (int i = 0; i < dict.length; i++) {
            dict[i] = gen();
        }
        //        System.out.println("dict size in mem:");
        //        MemoryMeterUtil.measure(dict);

        //for validate
        //printOri(dict);

        writeDict(dict);

        File file = new File("dict");
        try (ByteArrayInputStream bis = new ByteArrayInputStream(FileUtils.readFileToByteArray(file))) {

            DataInputStream in = new DataInputStream(bis);
            int len = in.readInt();

            int[] pos = new int[len];
            for (int i = 0; i < pos.length; i++) {
                pos[i] = in.readInt();
            }

            System.out.println(get(in, pos, 50000));


//            System.out.println("--------------");
//            System.out.println(get(in, pos, 50));
//            System.out.println(get(in, pos, 51));
            in.close();
        }
        FileUtils.deleteQuietly(file);
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

    public static String get(DataInputStream in, int[] pos, int n) throws IOException {
        long t1 = System.currentTimeMillis();
        int p = pos[n - 1];
        int l = pos[n] - p;
        byte[] r = new byte[l];
        in.mark(l);
        in.skip(p);
        in.read(r);
        in.reset();
        String s = new String(r, Charset.forName("UTF-8"));
        System.out.println("duration:" + (System.currentTimeMillis() - t1));
        return s;
    }

    public static String gen() {
        return RandomStringUtils.randomAlphanumeric(new Random().nextInt(100) + 1);
    }
}

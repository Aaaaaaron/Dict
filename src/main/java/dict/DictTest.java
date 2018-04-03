package dict;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Random;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.RandomStringUtils;

public class DictTest {
    public static void main(String[] args) throws IOException {
        String[] dict = new String[1000*1000];
        for (int i = 0; i < dict.length; i++) {
            dict[i] = gen();
        }
/*
        //validate
        int total = 0;
        for (int i = 0; i < 100; i++) {
            int currentLen = dict[i].getBytes(Charset.forName("UTF-8")).length;
            System.out.println("[" + i + "]:[" + (total += currentLen) + "]" + currentLen);
            System.out.println(dict[i]);
        }
*/
//        writeDict(dict);

        try(InputStream fis = new FileInputStream("dict")) {
            DataInputStream in = new DataInputStream(fis);
            int len = in.readInt();
            int[] pos = new int[len];
            for (int i = 0; i < pos.length; i++) {
                pos[i] = in.readInt();
            }

            System.out.println("--------------");
            System.out.println(get(in, pos, 50));
            System.out.println(get(in, pos, 51));

        }
        System.out.println("--------------");



/*        int pre = 0;
        for (int i = 0; i < 10; i++) {
            int curr = pos[i];
            byte[] r = new byte[curr - pre];
            in.read(r);
            System.out.println(new String(r, Charset.forName("UTF-8")));
            pre = curr;
        }*/

    }

    private static void  writeDict(String[] dict) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DictHeader header = new DictHeader(dict);
        DictValue value = new DictValue(dict);
        DictHeader.DictHeaderSerializer.DICT_HEADER_SERIALIZER.serialize(header, new DataOutputStream(out));
        DictValue.DictValueSerializer.DICT_VALUE_SERIALIZER.serialize(value, new DataOutputStream(out));

        try(OutputStream outputStream = new FileOutputStream("dict")) {
            out.writeTo(outputStream);
            out.close();
        }
    }

    private static String get(DataInputStream in, int[] pos, int n) throws IOException {
        int p = pos[n-1];
        int l = pos[n] - p;
        byte[] r = new byte[l];
        in.mark(l);
        in.skip(p);
        in.read(r);
        in.reset();
        return new String(r, Charset.forName("UTF-8"));
    }

    private static String gen() {
        return RandomStringUtils.randomAlphanumeric(new Random().nextInt(100) + 1);
    }
/*
    public static Pair<Integer, Integer> pos(byte[] b) throws IOException {
        byte[] s = new byte[4];
        System.arraycopy(b, 0, s, 0, 4);
        byte[] e = new byte[4];
        System.arraycopy(b, 4, e, 0, 4);
        return Pair.newPair(readInt(s), readInt(e));
    }

    public static final int readInt(byte[] b) throws IOException {
        int ch1 = b[0];
        int ch2 = b[1];
        int ch3 = b[2];
        int ch4 = b[3];
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }*/
}

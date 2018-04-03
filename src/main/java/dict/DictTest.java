package dict;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

public class DictTest {
    private int CAP = 1000 * 1000;

    public void write(String dictName) throws IOException {
        String[] dict = new String[CAP];
        for (int i = 0; i < dict.length; i++) {
            dict[i] = gen();
        }
        writeDict(dict, dictName);
    }

    @Test
    public void testMemory() {
        String[] dict = new String[1000 * 1000];
        for (int i = 0; i < dict.length; i++) {
            dict[i] = gen();
        }
        MemoryMeterUtil.measure(dict);
    }

    @Test
    public void read() throws IOException {
        String dictName = "dict";
        write(dictName);

        File file = new File(dictName);
        try (RandomAccessFile r = new RandomAccessFile(file, "r")) {
            int len = r.readInt();

            int[] pos = new int[len];
            for (int i = 0; i < pos.length; i++) {
                pos[i] = r.readInt();
            }

            long t1 = System.currentTimeMillis();
            for (int i = 0; i < CAP; i++) {
                get(r, pos, new Random().nextInt(CAP));
            }
            System.out.println("duration:" + (System.currentTimeMillis() - t1));
        }
    }

    @Test
    public void testSense() throws IOException {
//        simulation(1, 1_000_000); // baseline duration:3621
//        simulation(1, 10_000); // baseline duration:3487
//        simulation(3, 10_000); // duration:10746
//        simulation(3, 10_0000); // duration:10579
//        simulation(6, 10_000); // duration:24899
//        simulation(10, 10_000); // duration:48514
//        simulation(20, 10_000); // duration:103165
//        simulation(30, 10_000); // duration:146469
     }

    public void simulation(int cols, int readOnetime) throws IOException {
        List<RandomAccessFile> ins = new ArrayList<>(cols);
        for (int i = 0; i < cols; i++) {
            String dictName = "dict" + i;
            write(dictName);
            ins.add(getInputStream(dictName));
        }

        List<int[]> headers = new ArrayList<>(cols);

        for (int i = 0; i < cols; i++) {
            headers.add(getHeader(ins.get(i)));
        }

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < CAP / readOnetime; i++) { // rounds
            for (int k = 0; k < cols; k++) { // simulation n cols
                for (int j = 0; j < readOnetime; j++) { // n rows per col and round
                    get(ins.get(k), headers.get(k), new Random().nextInt(CAP));
                }
            }
        }
        System.out.println("duration:" + (System.currentTimeMillis() - t1));
    }

    private int[] getHeader(RandomAccessFile in) throws IOException {
        int len = in.readInt();
        int[] pos = new int[len];
        for (int i = 0; i < pos.length; i++) {
            pos[i] = in.readInt();
        }
        return pos;
    }

    private RandomAccessFile getInputStream(String dictName) throws FileNotFoundException {
        File file = new File(dictName);
        return new RandomAccessFile(file, "r");
    }

    private static void printOri(String[] dict) {
        int total = 0;
        for (int i = 0; i < 100; i++) {
            int currentLen = dict[i].getBytes(Charset.forName("UTF-8")).length;
            System.out.println("[" + i + "]:[" + (total += currentLen) + "]" + currentLen);
            System.out.println(dict[i]);
        }
    }

    private static void writeDict(String[] dict, String name) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DictHeader header = new DictHeader(dict);
        DictValue value = new DictValue(dict);
        DictHeader.DictHeaderSerializer.DICT_HEADER_SERIALIZER.serialize(header, new DataOutputStream(out));
        DictValue.DictValueSerializer.DICT_VALUE_SERIALIZER.serialize(value, new DataOutputStream(out));

        try (OutputStream outputStream = new FileOutputStream(name)) {
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
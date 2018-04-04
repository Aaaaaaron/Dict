package dict;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Ignore;
import org.junit.Test;

public class DictTestForHDFS {
    private int CAP = 1000 * 1000;

    public void write(String dictName) throws IOException {
        String[] dict = new String[CAP];
        for (int i = 0; i < dict.length; i++) {
            dict[i] = gen();
        }
        writeDict(dict, dictName);
//        printOri(dict);
    }

    @Test
    @Ignore
    public void testMemory() {
        String[] dict = new String[1000 * 1000];
        for (int i = 0; i < dict.length; i++) {
            dict[i] = gen();
        }
        MemoryMeterUtil.measure(dict);
    }

    @Test
    public void read() throws IOException {
        CAP = 1000;
        String dictName = "dict";
        write(dictName);

        Path p = new Path("/Users/jiatao.tao/Documents/test_dict/" + dictName + ".dict");
        try (FSDataInputStream fis = p.getFileSystem(new Configuration()).open(p)) {

            int len = fis.readInt();
            int[] pos = new int[len];
            for (int i = 0; i < pos.length; i++) {
                pos[i] = fis.readInt();
            }

            long t1 = System.currentTimeMillis();
            System.out.println("------------");
            System.out.println(get(fis, pos, 0));
            System.out.println(get(fis, pos, 50));
            System.out.println(get(fis, pos, 51));
            System.out.println(get(fis, pos, 49));

            /*
            for (int i = 0; i < CAP; i++) {
                get(buffer, pos, new Random().nextInt(CAP));
            }
            */
            System.out.println("duration:" + (System.currentTimeMillis() - t1));
        }
    }

    @Test
    public void testSense() throws IOException {
        simulation(1, 1_000_000); // baseline duration:372
//        simulation(1, 10_000); // baseline duration:355
//        simulation(3, 10_000); // duration:1116
/*        simulation(3, 10_0000); // duration:1099
        simulation(6, 10_000); // duration:2282
        simulation(10, 10_000); // duration:4032
        simulation(20, 10_000); // duration:7835
        simulation(30, 10_000); // duration:12096
        simulation(50, 10_000); // duration:20335*/
    }

    public void simulation(int cols, int readOnetime) throws IOException {
        List<FSDataInputStream> in = new ArrayList<>(cols);
        for (int i = 0; i < cols; i++) {
            String dictName = "dict" + i;
            write(dictName);
            in.add(getFSDataInputStream(dictName));
        }

        List<int[]> headers = new ArrayList<>(cols);

        for (int i = 0; i < cols; i++) {
            headers.add(getHeader(in.get(i)));
        }

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < CAP / readOnetime; i++) { // rounds
            for (int k = 0; k < cols; k++) { // simulation n cols
                for (int j = 0; j < readOnetime; j++) { // n rows per col and round
                    get(in.get(k), headers.get(k), new Random().nextInt(CAP));
                }
            }
        }
        System.out.println("duration:" + (System.currentTimeMillis() - t1));
    }

    private int[] getHeader(FSDataInputStream buf) throws IOException {
        int len = buf.readInt();
        int[] pos = new int[len];
        for (int i = 0; i < pos.length; i++) {
            pos[i] = buf.readInt();
        }
        return pos;
    }

    private FSDataInputStream getFSDataInputStream(String dictName) throws IOException {
        Path p = new Path("/Users/jiatao.tao/Documents/test_dict/" + dictName + ".dict");
        return p.getFileSystem(new Configuration()).open(p);
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
        Path p = new Path("/Users/jiatao.tao/Documents/test_dict/" + name + ".dict");
        FileSystem fs = p.getFileSystem(new Configuration());
        fs.delete(p, true);
        FSDataOutputStream fso = fs.create(p);

        DictHeader header = new DictHeader(dict);
        DictValue value = new DictValue(dict);
        DictHeader.DictHeaderSerializer.DICT_HEADER_SERIALIZER.serialize(header, new DataOutputStream(fso));
        DictValue.DictValueSerializer.DICT_VALUE_SERIALIZER.serialize(value, new DataOutputStream(fso));
        fso.close();
    }

    public static String get(FSDataInputStream in, int[] pos, int n) throws IOException {
        byte[] r;
        if (n == 0) {
            r = new byte[pos[0]];
            in.read(r);
        } else {
            int base = 4 * pos.length + 4;
            int p = pos[n - 1];
            int l = pos[n] - p;
            r = new byte[l];
            in.seek(p + base);
            in.read(r);
        }
        return new String(r, Charset.forName("UTF-8"));
    }

    public static String gen() {
        return RandomStringUtils.randomAlphanumeric(new Random().nextInt(100) + 1);
    }
}
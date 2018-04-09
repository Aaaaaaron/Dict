package test;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import dict.SDict;

public class SDictTest {
    @Test
    public void testGet() throws IOException {
        String[] dict = new String[100];
        for (int i = 0; i < dict.length; i++) {
            dict[i] = gen();
        }

        SDict w = new SDict(dict);
        File f = File.createTempFile("dict", ".dict");
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(f))) {
            w.write(out);
        }

        RandomAccessFile in = new RandomAccessFile(f, "r");
        SDict r = new SDict();
        r.threadSafeRead(in);

        Assert.assertArrayEquals(dict[99].getBytes(), r.get(99));
        Assert.assertArrayEquals(dict[99].getBytes(), r.get(99));
        Assert.assertArrayEquals(dict[99].getBytes(), r.get(99));

        Assert.assertArrayEquals(dict[50].getBytes(), r.threadSafeGet(50));
        Assert.assertArrayEquals(dict[50].getBytes(), r.threadSafeGet(50));
        Assert.assertArrayEquals(dict[50].getBytes(), r.threadSafeGet(50));

/*        Assert.assertArrayEquals(dict[0].getBytes(), r.get3(0));
        Assert.assertArrayEquals(dict[0].getBytes(), r.get3(0));
        Assert.assertArrayEquals(dict[0].getBytes(), r.get3(0));*/
    }

    @Test
    public void testMultiThread() throws IOException, InterruptedException {
        int cap = 1000 * 1000;
        String[] dict = new String[cap];
        for (int i = 0; i < dict.length; i++) {
            dict[i] = gen();
        }

        SDict w = new SDict(dict);
        File f = File.createTempFile("dict", ".dict");
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(f))) {
            w.write(out);
        }
        System.out.println("write down.");

        RandomAccessFile in = new RandomAccessFile(f, "r");
        SDict r = new SDict();
        r.read(in);

        int nThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++){
            executorService.execute(new TestRunnable(r, dict));
        }

        Thread.sleep(1000 * 200);
        executorService.shutdown();
    }

    class TestRunnable implements Runnable{
        private SDict d;
        private String[] ori;

        public TestRunnable(SDict r, String[] ori) {
            this.d = r;
            this.ori = ori;
        }

        public void run(){
            int index = new Random().nextInt(1000 * 1000);
            Assert.assertArrayEquals(ori[index].getBytes(), d.threadSafeGet(index));
        }
    }

    @Test
    @Ignore
    public void benchmark() throws IOException {
        int cap = 1000 * 1000;
        String[] dict = new String[cap];
        for (int i = 0; i < dict.length; i++) {
            dict[i] = gen();
        }

        SDict w = new SDict(dict);
        File f = File.createTempFile("dict", ".dict");
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(f))) {
            w.write(out);
        }

        RandomAccessFile in = new RandomAccessFile(f, "r");
        SDict r = new SDict();
        r.read(in);


        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 1000000000; i++) {
            r.get(new Random().nextInt(cap));
        }
        System.out.println("duration:" + (System.currentTimeMillis() - t1));

        long t2 = System.currentTimeMillis();
        for (int i = 0; i < 1000000000; i++) {
            r.threadSafeGet(new Random().nextInt(cap));
        }
        System.out.println("duration:" + (System.currentTimeMillis() - t2));
    }

    private static String gen() {
        return RandomStringUtils.randomAlphanumeric(new Random().nextInt(100) + 1);
    }
}

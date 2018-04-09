package test;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
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
        r.read(in);

        Assert.assertArrayEquals(dict[99].getBytes(), r.getValueBytesFromIdImpl(99));
        Assert.assertArrayEquals(dict[50].getBytes(), r.getValueBytesFromIdImpl(50));
        Assert.assertArrayEquals(dict[0].getBytes(), r.getValueBytesFromIdImpl(0));
    }

    private static String gen() {
        return RandomStringUtils.randomAlphanumeric(new Random().nextInt(100) + 1);
    }
}

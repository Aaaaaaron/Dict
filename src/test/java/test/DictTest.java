package test;

import dict.DictHeader;
import dict.DictValue;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import static dict.DictTest.gen;
import static dict.DictTest.get;

public class DictTest {
    public static void main(String[] args) throws IOException {
        String[] dict = new String[1000 * 1000];
        for (int i = 0; i < dict.length; i++) {
            dict[i] = gen();
        }

        int total = 0;
        for (int i = 0; i < 100; i++) {
            int currentLen = dict[i].getBytes(Charset.forName("UTF-8")).length;
            System.out.println("[" + i + "]:[" + (total += currentLen) + "]" + currentLen);
            System.out.println(dict[i]);
        }

        ByteArrayOutputStream headerOut = new ByteArrayOutputStream();
        DictHeader header = new DictHeader(dict);
        DictValue value = new DictValue(dict);
        DictHeader.DictHeaderSerializer.DICT_HEADER_SERIALIZER.serialize(header, new DataOutputStream(headerOut));
        DictValue.DictValueSerializer.DICT_VALUE_SERIALIZER.serialize(value, new DataOutputStream(headerOut));

        byte[] bytes = headerOut.toByteArray();

        System.out.println("--------------");

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(bis);
        int len = in.readInt();
        int[] pos = new int[len];
        for (int i = 0; i < pos.length; i++) {
            pos[i] = in.readInt();
        }

        System.out.println("--------------");
//        System.out.println(get(in, pos, 50));
//        System.out.println(get(in, pos, 51));

    }
}

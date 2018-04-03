package dict;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class DictHeader {
    private int[] length;

    public DictHeader(String[] dict) {
        int total = 0;
        this.length = new int[dict.length];
        for (int i = 0; i < dict.length; i++) {
            int currentLen = dict[i].getBytes(Charset.forName("UTF-8")).length;
            this.length[i] = (total += currentLen);
        }
    }

    public int[] getLength() {
        return length;
    }

    public static class DictHeaderSerializer {

        public static final DictHeaderSerializer DICT_HEADER_SERIALIZER = new DictHeaderSerializer();

        public void serialize(DictHeader obj, DataOutputStream out) throws IOException {
            int[] lengths = obj.getLength();
            out.writeInt(lengths.length);
            for (int length : lengths) {
                out.writeInt(length);
            }
        }

/*
        public DictHeader deserialize(DataInputStream in) throws IOException {
            int length = in.readInt();
            int[] offsets = new int[length];
            for (int i = 0; i < length; i++) {
                offsets[i] = in.readInt();
            }

            return new DictHeader(offsets);
        }
*/

    }


}
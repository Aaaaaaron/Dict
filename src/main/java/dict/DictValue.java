package dict;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class DictValue {
    private String[] values;

    public DictValue(String[] values) {
        this.values = values;
    }

    public static class DictValueSerializer {

        public static final DictValueSerializer DICT_VALUE_SERIALIZER = new DictValueSerializer();

        public void serialize(DictValue obj, DataOutputStream out) throws IOException {
            for (String value : obj.values) {
                out.write(value.getBytes(Charset.forName("UTF-8")));
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
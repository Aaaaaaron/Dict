/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dict;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

// dict for query
public class SDict {
    private int[] pos;
    private MappedByteBuffer byteBuffer;

    // only need when write dict
    transient private String[] values;

    public SDict() { // default constructor for Writable interface
    }

    public SDict(String[] values) {
        init(values);
    }

    private void init(String[] values) {
        int total = 0;
        this.values = values;
        this.pos = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            int currentLen = values[i].getBytes().length;
            this.pos[i] = (total += currentLen);
        }
    }

    public int getMinId() {
        return 0;
    }

    public int getMaxId() {
        return pos.length - 1;
    }

    public int getSizeOfId() {
        return 4; //size of int
    }

    public byte[] getValueBytesFromIdImpl(int id) {
        return get(id);
    }

    public void write(DataOutput out) throws IOException {
        // write head
        out.writeInt(pos.length);
        for (int length : pos) {
            out.writeInt(length);
        }

        // write body
        for (String value : values) {
            out.write(value.getBytes());
        }
    }

    public void read(DataInput in) throws IOException {
        FileChannel fc = ((RandomAccessFile) in).getChannel();
        byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        int len = byteBuffer.getInt();

        pos = new int[len];
        for (int i = 0; i < pos.length; i++) {
            pos[i] = byteBuffer.getInt();
        }
    }

    private byte[] get(int id) {
        byte[] r;
        int base = 4 * pos.length + 4;
        if (id == 0) {
            r = new byte[pos[0]];
            byteBuffer.position(base);
            byteBuffer.get(r);
        } else {
            int p = pos[id - 1];
            int l = pos[id] - p;
            r = new byte[l];
            byteBuffer.position(p + base);
            byteBuffer.get(r);
        }
        return r;
    }
}

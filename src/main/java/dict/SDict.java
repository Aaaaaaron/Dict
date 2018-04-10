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

import java.io.DataOutput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import sun.nio.ch.FileChannelImpl;

// dict for query
public class SDict implements CloseableSizedFile {
    private int[] pos;
    private MappedByteBuffer byteBuffer;

    // only need when write dict
    private String[] values;

    // keep for closing
    private RandomAccessFile raf;
    private FileChannel fc;

    public SDict() { // default constructor for Writable interface
    }

    public SDict(RandomAccessFile in) {
        raf = in;
    }

    public SDict(String[] values) {
        int total = 0;
        this.values = values;
        this.pos = new int[this.values.length];
        for (int i = 0; i < this.values.length; i++) {
            int currentLen = this.values[i].getBytes().length;
            this.pos[i] = (total += currentLen);
        }
    }

    // lazy file mapping.
    @Override
    public void init() {
        try {
            fc = raf.getChannel();
            byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

            int len = byteBuffer.getInt();
            pos = new int[len];
            for (int i = 0; i < pos.length; i++) {
                pos[i] = byteBuffer.getInt();
            }
        } catch (Exception e) {
            throw new RuntimeException("Can not init sdict.", e);
        }
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

    @Override
    public long getSizeInBytes() {
        try {
            return raf.length();
        } catch (IOException e) {
            throw new RuntimeException("Can not get file length.", e);
        }
    }

    // thread safe
    public byte[] get(int id) {
        byte[] r;
        int base = 4 * pos.length + 4;
        int index;
        try {
            if (id == 0) {
                r = new byte[pos[0]];
                index = base;
            } else {
                int p = pos[id - 1];
                int l = pos[id] - p;
                r = new byte[l];
                index = p + base;
            }
            for (int i = 0; i < r.length; i++) {
                r[i] = byteBuffer.get(index + i);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
        return r;
    }

    /*
    
    public byte[] get2(int id) {
        byte[] r;
        int base = 4 * pos.length + 4;
        int index;
        if (id == 0) {
            r = new byte[pos[0]];
            index = base;
        } else {
            int p = pos[id - 1];
            int l = pos[id] - p;
            r = new byte[l];
            index = p + base;
        }
        byteBuffer.position(index);
        byteBuffer.get(r);
        return r;
    }
    
    public byte[] get3(int id) {
        byte[] r;
        int base = 4 * pos.length + 4;
        int index;
        if (id == 0) {
            r = new byte[pos[0]];
            index = base;
        } else {
            int p = pos[id - 1];
            int l = pos[id] - p;
            r = new byte[l];
            index = p + base;
        }
    
        Bits.swap(Platform.getInt(byteBuffer.));
        for (int i = 0; i < r.length; i++) {
            r[i] = byteBuffer.get(index + i);
        }
        return r;
    }
    */

    @Override
    public void close() {
        try {
            fc.close();
            raf.close();
            Method m = FileChannelImpl.class.getDeclaredMethod("unmap", MappedByteBuffer.class);
            m.setAccessible(true);
            m.invoke(FileChannelImpl.class, byteBuffer);
        } catch (Exception e) {
            throw new RuntimeException("Can not release file mapping memory.", e);
        }
    }
}
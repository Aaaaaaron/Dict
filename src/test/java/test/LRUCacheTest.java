/*
 * Copyright (C) 2016 Kyligence Inc. All rights reserved.
 *
 * http://kyligence.io
 *
 * This software is the confidential and proprietary information of
 * Kyligence Inc. ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with
 * Kyligence Inc.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package test;

import dict.SDict;
import org.junit.Assert;
import org.junit.Test;

import dict.LRUCache;

public class LRUCacheTest {
    @Test
    public void testBasic() {
        LRUCache<String, MockSDict> cache = new LRUCache<>(1000);
        MockSDict a = new MockSDict(100);
        MockSDict b = new MockSDict(200);
        MockSDict c = new MockSDict(300);

        cache.put("a", a);
        cache.put("b", b);
        cache.put("c", c);

        Assert.assertEquals(a, cache.getIfPresent("a"));
        Assert.assertEquals(c, cache.getIfPresent("c"));

        Assert.assertEquals(1, b.getInitCount());
        Assert.assertEquals(1, a.getInitCount());
        Assert.assertEquals(1, c.getInitCount());

        Assert.assertEquals(0, a.getCloseCount());
        Assert.assertEquals(0, b.getCloseCount());
        Assert.assertEquals(0, c.getCloseCount());

        MockSDict d = new MockSDict(500);
        cache.put("d", d);
        Assert.assertEquals(3, cache.size());

        Assert.assertEquals(0, a.getCloseCount());
        Assert.assertEquals(1, b.getCloseCount());
        Assert.assertEquals(0, c.getCloseCount());
        Assert.assertNull(cache.getIfPresent("b"));

        MockSDict e = new MockSDict(1000);
        cache.put("e", e);
        Assert.assertEquals(1, cache.size());
        Assert.assertEquals(e, cache.getIfPresent("e"));

        try {
            MockSDict f = new MockSDict(1100);
            cache.put("f", f);
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertEquals("dict max size in mem is too small", ex.getMessage());
        }

    }

    @Test
    public void testMultiThread() {

    }

    static class MockSDict extends SDict {
        int initCount = 0;
        int closeCount = 0;
        int size = 0;

        MockSDict(int size) {
            this.size = size;
        }

        @Override
        public void init() {
            initCount++;
        }

        @Override
        public long getSizeInBytes() {
            return size;
        }

        @Override
        public void close() {
            closeCount++;
        }

        public int getInitCount() {
            return initCount;
        }

        public int getCloseCount() {
            return closeCount;
        }
    }
}

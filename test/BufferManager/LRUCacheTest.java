/*
 * The MIT License
 *
 * Copyright 2015 Shamil Garifullin <shamil.garifullin at mit.spbau>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package BufferManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Shamil Garifullin <shamil.garifullin at mit.spbau>
 */
public class LRUCacheTest {
    
    LRUCache<Integer, Integer> lra;
    
    public LRUCacheTest() {
    }
    
    @Before
    public void setUp() {
        lra = new LRUCache<>(5);
    }

    @Test
    public void testCapacity() {
        Integer k = null;
        for (int i = 0; i < 12; ++i){
            k = new Integer(i);
            lra.put(k, k);
        }
        assertTrue(lra.containsKey(11));
        assertTrue(lra.containsKey(7));
        lra.remove(8);
        assertTrue(!lra.containsKey(8));
        lra.put(12, 12);
        assertTrue(lra.containsKey(7));
    }
    
}

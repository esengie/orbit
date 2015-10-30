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
package Utils;

import java.nio.ByteBuffer;

/**
 *
 * @author Shamil Garifullin <shamil.garifullin at mit.spbau>
 */
public class Bits {
    public static Bits fromBytes(ByteBuffer buf, int num){
        byte[] by = new byte[buf.remaining()];
        buf.get(by);
        boolean[] res = new boolean[num];
        for (int i = 0; i < by.length; ++i){
            byte b = by[i];
            for (int j = 0; j < 8; ++j) {
                if ((b & (1 << j)) > 0){
                    if (i*8+j == num) break;
                    res[i*8+j] = true;
                }
            }
        }
        return new Bits(res);
    }
    public static byte[] toByteArray(Bits b){
        byte[] by = new byte[(b.raw.length+7)/8];
        for (int i = 0; i < by.length; ++i){
            for (int j = 0; j < 8; ++j) {
                if (i*8+j == b.raw.length) break;
                if (b.raw[i*8+j]){
                    by[i] |= 1 << j;
                }
            }
        }
        return by;
    }
    private boolean[] raw;
    private Bits (boolean[] b){
        raw = b;
    }
    public int length(){
        return raw.length;
    }
    public void clear(){
        raw = new boolean[raw.length];
    }
    public int nextClearBit(int i){
        while(i < raw.length && raw[i] != true){
            ++i;
        }
        if (i == raw.length)
            return -1;
        return i;
    }
    public int nextSetBit(int i){
        while(i < raw.length && raw[i] == true){
            ++i;
        }
        if (i == raw.length)
            return -1;
        return i;
    }
    public void set(int i){
        raw[i] = true;
    }
    public void clear(int i){
        raw[i] = false;
    }
}

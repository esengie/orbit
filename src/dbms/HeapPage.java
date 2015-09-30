/*
 * The MIT License
 *
 * Copyright 2015 esengie.
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
package dbms;

import java.util.BitSet;

/**
 *
 * @author esengie
 */
public class HeapPage extends Page{
    private int recordSize;
    private int free;
    private int slots_position;
    BitSet slots;
    
    HeapPage(int i, int rSize){
        super(i);
        recordSize = rSize;
        free = getFree();
        slots_position = getSize() - (slots.size() + 7) / 8;
        buff.position(slots_position);
        slots = BitSet.valueOf(buff);
    }
    
    void create(){
        int slots_size = free / (recordSize + 1);  //because need to store this bitset also
        free -= (slots.size() + 7) / 8;
        setFree(free);
    }
//    private void putInt(int k){
//        buff.putInt(k);
//    }
//    public void putLong(long i){
//        buff.putLong(i);
//    }
    private void setBits(){
        buff.position(slots_position);
        buff.put(slots.toByteArray());
    }
}

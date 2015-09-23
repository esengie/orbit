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
package dbms;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 *
 * @author Shamil Garifullin <shamil.garifullin at mit.spbau>
 */
public class Page {
    protected static final int SIZE = 4096;
    private final int Id;
    private int free = 4096 - 4 - 4 - 1 - 4; // prev next deleted free
    protected ByteBuffer buff;
    Page(int id) {
        Id = id;
        buff = ByteBuffer.allocate(4096);
        buff.position(17);
    }
    void put(ByteBuffer src){
        buff.put(src);
    }
    public int getId(){
        return Id;
    }
    public int getFree(){
        return free;
    }
    public int getSize(){
        return SIZE;
    }
    private void updateFree(){
        set(free, 9);
    }
    private void set(int k, int pos){
        buff.putInt(pos, k);
    }
    void setPrev(int k){
        set(k, 0);
    }
    void setNext(int k){
        set(k, 4);
    }
    void setDeleted(){
        byte l = 1;
        buff.put(5, l);
    }    
}

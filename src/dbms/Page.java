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

/**
 *
 * @author Shamil Garifullin <shamil.garifullin at mit.spbau>
 */
public class Page {

    private static final int PREV = 0;
    private static final int NEXT = 4;
    private static final int SIZE = 4096;
    private static final int DELETED = 8;
    private static final int FREE = 9;
    private sta
    private final int Id;
    
    protected ByteBuffer buff;

    Page(int id) {
        Id = id;
        buff = ByteBuffer.allocate(SIZE);
    }

    public int getId() {
        return Id;
    }
    public int getSize() {
        return SIZE;
    }
    protected void setInt(int pos, int k) {
        buff.putInt(pos, k);
    }
    protected int getInt(int pos) {
        return buff.getInt(pos);
    }
    protected void setPrev(int k) {
        setInt(PREV, k);
    }
    protected int getPrev() {
        return getInt(PREV);
    }
    protected void setNext(int k) {
        setInt(NEXT, k);
    }
    protected int getNext() {
        return getInt(NEXT);
    }
    protected void setFree(int f){
        setInt(FREE, f);
    }
    // do not ever use this!
    protected void setInitialFree(){
        setInt(FREE, SIZE - (FREE + 4));
    }
    protected int getFree(){
        return getInt(FREE);
    }
    protected void setDeleted(){
        byte l = 1;
        buff.put(DELETED, l);
    }
    protected void unsetDeleted(){
        byte l = 0;
        buff.put(DELETED, l);
    }
}

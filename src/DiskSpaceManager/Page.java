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
package DiskSpaceManager;

import SettingsAndMeta.GlobalConsts;
import java.nio.ByteBuffer;

/**
 *
 * @author Shamil Garifullin <shamil.garifullin at mit.spbau>
 */
public class Page extends GlobalConsts {
    private final int Id;
    
    public ByteBuffer buff;

    public Page(int id) {
        Id = id;
        buff = ByteBuffer.allocate(PAGE_SIZE);
    }
    
    public int getId() {
        return Id;
    }
    public int getSize() {
        return PAGE_SIZE;
    }
    protected void setInt(int pos, int k) {
        buff.putInt(pos, k);
    }
    protected int getInt(int pos) {
        return buff.getInt(pos);
    }
    public void setPrev(int k) {
        setInt(PAGE_PREV, k);
    }
    public int getPrev() {
        return getInt(PAGE_PREV);
    }
    public void setNext(int k) {
        setInt(PAGE_NEXT, k);
    }
    public int getNext() {
        return getInt(PAGE_NEXT);
    }
    public void commitFree(int free){
        if (free > PAGE_SIZE - page_offset){
            throw new IllegalStateException("Free is too big");
        }
        setInt(PAGE_FREE, free);
    }
    // do not ever use this!
    public void setInitialFree(){
        setInt(PAGE_FREE, PAGE_SIZE - page_offset);
    }
    public int getFree(){
        return getInt(PAGE_FREE);
    }
    public boolean getDeleted(){
        return buff.get(PAGE_DELETED) != 0;
    }
    public void setDeleted(){
        byte l = 1;
        buff.put(PAGE_DELETED, l);
    }
    public void clearDeleted(){
        byte l = 0;
        buff.put(PAGE_DELETED, l);
    }
}

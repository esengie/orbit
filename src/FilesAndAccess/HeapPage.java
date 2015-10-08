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
package FilesAndAccess;

import BufferManager.BufferManager;
import DiskSpaceManager.Page;
import SettingsAndMeta.GlobalConsts;
import SettingsAndMeta.Schema;
import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 *
 * @author esengie
 */
public class HeapPage extends GlobalConsts{
    // Simple fixed length slots given by recordSize from Record
    // Also stores a BitSet in the end of a Page
    protected final int pid;
    private final int recordSize;
    private final int slotsPosition;
    private final BitSet slots;
    private int free;
    
    BufferManager buf;
    
    public HeapPage(int pageNum, int rSize, BufferManager b){
        pid = pageNum;
        recordSize = rSize;
        buf = b;
        Page p = buf.getPage(pid);
        free = p.getFree();
        // + 1 because need to store this bitset also
        int number_of_slots = (p.getSize() - Page.page_offset) * 8 / (8 * recordSize + 1); 
        slotsPosition = p.getSize() - (number_of_slots + 7) / 8;
        p.buff.position(slotsPosition);
        
        ByteBuffer buftmp = p.buff.duplicate();
        // not - 7, because we could have more bits than we have memory
        buftmp.limit (buftmp.position() + (number_of_slots) / 8); 
        slots = BitSet.valueOf(buftmp);
        
        buf.setDirty(pid);
        buf.unpin(pid);
    }    
    // Need this bc of memory in/out to create init BitSet
    // maybe in construtor with if deleted?
    public void create(){
        Page p = buf.getPage(pid);
        
        free -= p.getSize() - slotsPosition;
        p.commitFree(free);
        
        slots.clear();
        commitBits(p);
        
        buf.setDirty(pid);
        buf.unpin(pid);
    }
    public void insertRecord(Record rec){
        Page p = buf.getPage(pid);
        
        int i = slots.nextClearBit(0);
        p.buff.position(Page.page_offset + recordSize * i);
        rec.setRid(p.getId(), i);
        rec.buff.position(0);
        p.buff.put(rec.buff);
        free -= recordSize;
        slots.flip(i);
        p.commitFree(free);
        commitBits(p);
        
        buf.setDirty(pid);
        buf.unpin(pid);
    }
       
    public Record getRecord(Schema struc, Record.Rid rid){
        Record r = new Record(struc);
        Page p = buf.getPage(pid);

        p.buff.position(Page.page_offset + recordSize * rid.sid);

        ByteBuffer buftmp = p.buff.duplicate();
        buftmp.limit(buftmp.position() + recordSize);
        r.buff.put(buftmp);
        
        buf.unpin(pid);
        return r;
    }
    
    public void deleteRecord(Record.Rid rid){
        Page p = buf.getPage(pid);
        
        slots.flip(rid.sid);
        free += recordSize;
        p.commitFree(free);
        commitBits(p);
        
        buf.setDirty(pid);
        buf.unpin(pid);
    }
    public int getNextOccupiedSlot(int pos){
        return slots.nextSetBit(pos);
    }
    public int getFreeSlotsNum(){
        return free / recordSize;
    }
    public int getOccupiedSlotsNum(){
        return slots.size() - getFreeSlotsNum();
    }
    
    private void commitBits(Page p){
        p.buff.position(slotsPosition);
        p.buff.put(slots.toByteArray());
    }
    /// Use with caution
    /// used only for MetaPage
    protected void insertRecordAtPos(Record rec, int pos){
        Page p = buf.getPage(pid);
        
        p.buff.position(Page.page_offset + recordSize * pos);
//        rec.setRid(p.getId(), i);
        rec.buff.position(0);
        p.buff.put(rec.buff);
        free -= recordSize;
        slots.flip(pos);
        p.commitFree(free);
        commitBits(p);
        
        buf.setDirty(pid);
        buf.unpin(pid);
    }
    public int getNext(){
        Page p = buf.getPage(pid);
        int ret = p.getNext();
        buf.unpin(pid);
        return ret;
    }
    public int getPrev(){
        Page p = buf.getPage(pid);
        int ret = p.getPrev();
        buf.unpin(pid);
        return ret;
    }
    public void setNext(int id){
        Page p = buf.getPage(pid);
        p.setNext(id);
        
        buf.setDirty(pid);
        buf.unpin(pid);
    }
    public void setPrev(int id){
        Page p = buf.getPage(pid);
        p.setPrev(id);
        
        buf.setDirty(pid);
        buf.unpin(pid);
    }
}

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
package dbms.FilesAndAccess;

import dbms.BufferManager.BufferManager;
import dbms.SettingsAndMeta.RecordStructure;

/**
 *
 * @author esengie
 */
public class MetaPage extends HeapPage{
    private RecordStructure recStr;
    public MetaPage(int pageNum, BufferManager b){
        super(pageNum, META_PAGE_RECORD_SIZE, b);
        recStr = new RecordStructure();
        recStr.addField("data", "int");
        
    }
    public int getHalfFull(){
        return getInt(META_HALF_FULL_LOC);
    }
    public int getFull(){
        return getInt(META_FULL_LOC);
    }
    public void setFull(int id){
        setInt(id, META_FULL_LOC);
    }
    public void setHalfFull(int id){
        setInt(id, META_HALF_FULL_LOC);
    }
    private int getInt(int sid){
        Record rec = new Record(recStr);
        rec.setRid(pid, sid);
        return getRecord(recStr, rec.getRid()).buff.getInt();
    }
    private void setInt(int t, int pos){
        Record rec = new Record(recStr);
        rec.buff.putInt(t);
        insertRecordAtPos(rec, pos);
    }
}

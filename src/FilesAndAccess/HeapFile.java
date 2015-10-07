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
import DiskSpaceManager.DiskSpaceManager;
import SettingsAndMeta.Catalogue;
import SettingsAndMeta.RecordStructure;

/**
 *
 * @author esengie
 */
public class HeapFile {

    private final MetaPage metaPage;
    private final int myFull;
    private final int myPartial;
    private final RecordStructure recStr;

    DiskSpaceManager ptrDSM;
    BufferManager ptrBufM;
    Catalogue ptrCat;

    // if never existed
    // returns meta page id
    public static int create(DiskSpaceManager dsk, BufferManager buf) {
        int tmp = dsk.allocatePage();
        MetaPage metaPage = new MetaPage(tmp, buf);
        metaPage.create();
        int halfFull = dsk.allocatePage();
        int full = dsk.allocatePage();
        metaPage.setHalfFull(halfFull);
        metaPage.setFull(full);
        return tmp;
    }

    // if exists
    public HeapFile(DiskSpaceManager d, BufferManager b, int meta, RecordStructure rec) {
        ptrDSM = d;
        ptrBufM = b;
        metaPage = new MetaPage(meta, ptrBufM);
        recStr = rec;
        myFull = metaPage.getFull();
        myPartial = metaPage.getHalfFull();
    }

    public void insertRecord(Record rec) {
        HeapPage myP = getHeapPage(myPartial);
        HeapPage mine;
        if (myP.getNext() == myPartial){
            mine = getHeapPage(ptrDSM.allocatePage());
            mine.create();
            
            mine.setNext(myPartial);
            mine.setPrev(myPartial);
            myP.setNext(mine.pid);
            myP.setPrev(mine.pid);
            
            mine.insertRecord(rec);
        } else {      
            int mem = myP.getNext();
            mine = getHeapPage(mem);
            mine.insertRecord(rec);
            if (mine.getFreeSlotsNum() == 0){
                movePage(myFull, mine.pid);
            }
        }
    }

    private HeapPage getHeapPage(int pageNum){
        return new HeapPage(pageNum, recStr.getRecordSize(), ptrBufM);
    }
    public void deleteRecord(Record.Rid rid) {
        HeapPage p = getHeapPage(rid.pid);
        if (p.getFreeSlotsNum() == 0){
            movePage(myPartial, rid.pid);
        } else {
            if (p.getOccupiedSlotsNum() == 1){
                ptrDSM.deallocatePage(rid.pid);
                return;
            }
        }
        p.deleteRecord(rid);
    }
    private void movePage(int to, int pid){
        HeapPage toP = getHeapPage(to);
        HeapPage moved = getHeapPage(pid);
        
        HeapPage tmp = getHeapPage(moved.getNext());
        tmp.setPrev(moved.getPrev());
        tmp = getHeapPage(moved.getPrev());
        tmp.setNext(moved.getNext());
        
        moved.setNext(toP.getNext());
        moved.setPrev(to);
        
        toP.setNext(pid);
    }
    public void destroy() {
        HeapPage iter = getHeapPage(myFull);
        HeapPage tmp;
        while (myFull != iter.getNext()){
//            System.out.println(iter.getNext());
            tmp = getHeapPage(iter.getNext());
            iter.setNext(tmp.getNext());
            ptrDSM.deallocatePage(tmp.pid);
        }        
        iter = getHeapPage(myPartial);
        while (myPartial != iter.getNext()){
            tmp = getHeapPage(iter.getNext());
            iter.setNext(tmp.getNext());
            ptrDSM.deallocatePage(tmp.pid);
        }
        ptrDSM.deallocatePage(myFull);
        ptrDSM.deallocatePage(myPartial);
        ptrDSM.deallocatePage(metaPage.pid);
    }
}

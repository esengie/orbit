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
package FilesAndAccess;

import BufferManager.BufferManager;
import DiskSpaceManager.DiskSpaceManager;
import SettingsAndMeta.Schema;
import java.util.Iterator;

/**
 *
 * @author esengie
 */
public class HeapFile implements Iterable<Record> {

    private final MetaPage metaPage;
    protected final int myFull;
    protected final int myPartial;
    private Schema schema;
    private int myTotalRecs;

    DiskSpaceManager ptrDSM;
    BufferManager ptrBufM;

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema s) {
        if (schema.getRecordSize() == 0) {
            schema = s;
        } else {
            throw new IllegalAccessError("Schema was already set, this is a recipe for disaster!");
        }

    }

    @Override
    public Iterator<Record> iterator() {
        return new Scanner();
    }

    private class Scanner implements Iterator<Record> {

        private int curPid;
        private int curSid;
        private int prevPid;
        private int prevSid;
        private int notScanned;
        private boolean nexted = false;

        public Scanner() {
            notScanned = myTotalRecs;
            curPid = myPartial;
            curSid = 100000000;
            prevPid = curPid;
            prevSid = curSid;
            --notScanned;
            if (notScanned >= 0){
                moveForward();
            }
        }

        @Override
        public boolean hasNext() {
            return notScanned >= 0;
        }

        @Override
        public Record next() {
            if (hasNext()) {
                HeapPage p = getHeapPage(curPid);
                Record tmp = new Record(schema);
                tmp.setRid(curPid, curSid);
                --notScanned;
                nexted = true;
                tmp = p.getRecord(schema, tmp.getRid());
                if (hasNext()) {
                    moveForward();
                }
                return tmp;
            }
            throw new IllegalAccessError("Doesn't have next");
        }

        private void moveForward() {
            HeapPage p = getHeapPage(curPid);
            if (p.getNextOccupiedSlot(curSid + 1) == -1) {
                prevPid = curPid;
                curPid = p.getNext();
                if (curPid == myPartial) {
                    p = getHeapPage(myFull);
                    curPid = p.getNext();
                }
                if (curPid == myFull) {
                    throw new IllegalStateException(
                            "Something's fucked up in an iterator at step "
                            + String.valueOf(myTotalRecs - notScanned)
                            + " myTotalRecs is " + String.valueOf(myTotalRecs));
                }
                p = getHeapPage(curPid);
                prevSid = curSid;
                curSid = p.getNextOccupiedSlot(0);
            } else {
                prevPid = curPid;
                prevSid = curSid;
                curSid = p.getNextOccupiedSlot(curSid + 1);
            }
        }

        @Override
        public void remove() {
            if (!nexted) {
                throw new IllegalStateException("You must call next before this, baby");
            } else {
                Record tmp = new Record(schema);
                tmp.setRid(prevPid, prevSid);

                HeapPage p = getHeapPage(prevPid);
                p.deleteRecord(tmp.getRid());
                metaPage.setTotalRecs(--myTotalRecs);
                nexted = false;
            }
        }
    }

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
        metaPage.setTotalRecs(0);
        return tmp;
    }

    // if exists
    public HeapFile(DiskSpaceManager d, BufferManager b, int meta, Schema rec) {
        ptrDSM = d;
        ptrBufM = b;
        metaPage = new MetaPage(meta, ptrBufM);
        schema = rec;
        myFull = metaPage.getFull();
        myPartial = metaPage.getHalfFull();
        myTotalRecs = metaPage.getTotalRecs();
    }

    public boolean contains(Record rec) {
        for (Record item : this) {
            if (item.equals(rec)) {
                return true;
            }
        }
        return false;
    }

    public void insertRecord(Record rec) {
        HeapPage myP = getHeapPage(myPartial);
        HeapPage mine;
        if (myP.getNext() == myPartial) {
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
            if (mine.getFreeSlotsNum() == 0) {
                movePage(myFull, mine.pid);
            }
        }
        metaPage.setTotalRecs(++myTotalRecs);
    }

    public Record getRecord(Record.Rid rid) {
        HeapPage p = getHeapPage(rid.pid);
        return p.getRecord(schema, rid);
    }

    private HeapPage getHeapPage(int pageNum) {
        return new HeapPage(pageNum, schema.getRecordSize(), ptrBufM);
    }

    public void deleteRecord(Record.Rid rid) {
        HeapPage p = getHeapPage(rid.pid);
        p.deleteRecord(rid);
        if (p.getFreeSlotsNum() == 1) {
            movePage(myPartial, rid.pid);
        } else {
            if (p.getOccupiedSlotsNum() == 0) {
                p = wipeFromList(rid.pid);
                p.setDeleted();
            }
        }
        metaPage.setTotalRecs(--myTotalRecs);
    }

    // Эта функция нужна для remove с помощью итератора
    // Также наверное для индексов (хотя может буду его перестраивать)
    private void claimSpace() {
        throw new UnsupportedOperationException("Didn't implement claimspace yet");
    }

    private HeapPage wipeFromList(int pid) {
        HeapPage moved = getHeapPage(pid);
        HeapPage tmp = getHeapPage(moved.getNext());
        tmp.setPrev(moved.getPrev());
        tmp = getHeapPage(moved.getPrev());
        tmp.setNext(moved.getNext());

        return moved;
    }

    private void movePage(int to, int pid) {
        HeapPage toP = getHeapPage(to);
        HeapPage nexto = getHeapPage(toP.getNext());

        HeapPage moved = wipeFromList(pid);
        moved.setNext(toP.getNext());
        moved.setPrev(to);

        toP.setNext(pid);
        nexto.setPrev(pid);
    }

    public void destroy() {
        HeapPage iter = getHeapPage(myFull);
        HeapPage tmp;
        while (myFull != iter.getNext()) {
            int tt = iter.getNext();
            tmp = getHeapPage(tt);
            iter.setNext(tmp.getNext());
            tmp.setDeleted();
        }
        iter = getHeapPage(myPartial);
        while (myPartial != iter.getNext()) {
            tmp = getHeapPage(iter.getNext());
            iter.setNext(tmp.getNext());
            tmp.setDeleted();
        }
        tmp = getHeapPage(myFull);
        tmp.setDeleted();
        tmp = getHeapPage(myPartial);
        tmp.setDeleted();
        tmp = getHeapPage(metaPage.pid);
        tmp.setDeleted();
    }
}

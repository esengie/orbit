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
package dbms.DiskSpaceManager;

import dbms.SettingsAndMeta.GlobalConsts;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author Shamil Garifullin <shamil.garifullin at mit.spbau>
 */
public class DiskSpaceManager extends GlobalConsts {
    
    private int mSize = 1;
    private int freePages = 0;
    
    private RandomAccessFile mFile;
    private String fName;      // db name
    private Page fPage;       // info page

    public int allocatePage() {
        // doesn't care about prev, maybe fix later
        if (mSize < DBMS_FILE_MAX_SIZE || freePages > 0) {
            int firstFree = 0;
            try {
                if (0 == freePages) {
                    extendDb(EXTEND_DB);
                }
                Page local = readPage(fPage.getNext());
                firstFree = local.getId();
                fPage.setNext(local.getNext());
                --freePages;
            } catch (IOException ex) {
                throw new IllegalStateException("Read page error on allocate; allocate aborted", ex);
            }
            return firstFree;
        } else {
            throw new IllegalStateException("Not enough space left; allocate aborted");
        }
    }
    private void extendDb(int num) throws IOException {
        // updates free pool
        freePages += num;

        fPage.setNext(mSize);
        mFile.seek((long) (mSize * fPage.getSize()));
        mFile.writeInt(0); // set Prev

        for (int i = mSize + 1; i < mSize + num; ++i) {
            mFile.writeInt(i); // set Next
            mFile.writeInt(fPage.getFree());
            mFile.seek((long) (i * fPage.getSize()));
            mFile.writeInt(i); // set Prev
        }
        mSize += num;
        fPage.setPrev(mSize - 1);
    }
    public void deallocatePage(int pageId){
        if (pageId > 0 && pageId < mSize) {
            try {
                Page local = readPage(pageId);
                local.setNext(fPage.getNext());
                fPage.setNext(pageId);
                writePage(local);
                ++freePages;
            } catch (RuntimeException ex) {
                throw new IllegalStateException("Error during deallocation ", ex);
            }

        } else {
            throw new IllegalArgumentException("Invalid deallocate index out of bounds; aborted");
        }
    }

    public Page readPage(int pageId){
        if (pageId >= 0 && pageId < mSize) {
            try {
                Page p = new Page(pageId);
                mFile.seek((long) (pageId * p.getSize()));
                mFile.read(p.buff.array());
                return p;
            } catch (IOException ex) {
                throw new RuntimeException("Error while reading page");
            }
        } else {
            throw new IllegalArgumentException("Invalid page number; read aborted");
        }
    }
    public void writePage(Page page){
        if (page.getId() >= 0 && page.getId() < mSize) {
            try {
                mFile.seek((long) (page.getId() * page.getSize()));
                mFile.write(page.buff.array());
            } catch (IOException ex) {
                throw new RuntimeException("Error while writing page");
            }
        } else {
            throw new IllegalArgumentException("Invalid page number; write aborted");
        }

    }

    public void createDB(String f) {    // first page is reserved
        fName = f;
        try {
            mFile = new RandomAccessFile(fName, "rw");  //open connection
            fPage = readPage(0);
            fPage.setInitialFree();
        } catch (IOException except) {
            throw new RuntimeException("Well, suck my dick then! (creating db)");
        }
    }
    public void openDB(String f) {
        // Need many error checks many many many!!!
        fName = f;
        mSize = 1;
        try {
            mFile = new RandomAccessFile(fName, "rw");
            fPage = readPage(0);
        } catch (IOException except) {
            throw new RuntimeException("Well, suck my dick then! (opening db)");
        }
    }
    public void closeDB() {
        /// Write the control page
        try {
            // neeeeeed to flush !!!!
            writePage(fPage);
            mFile.close();
        } catch (IOException ex) {
            throw new RuntimeException("Well, suck my dick then! (flushing to disk before exit)");
        }
    }
    public void deleteDB() {
        closeDB();
        File var1 = new File(fName);
        var1.delete();
    }
}

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
            int ret = 0;
            try {
                if (0 == freePages) {
                    extendDb(EXTEND_DB);
                }
                Page local = readPage(fPage.getNext());
                ret = local.getId();
                
                fPage.setNext(local.getNext());
                // To be clean
                local.setNext(ret);
                local.setPrev(ret);
                local.setInitialFree();
                writePage(local);
                
                // setting prev
                local = readPage(fPage.getNext());
                local.setPrev(fPage.getId());
                writePage(local);
                
                --freePages;
                fPage.setInt(FREE_PAGES, freePages);
            } catch (IOException ex) {
                throw new IllegalStateException("Read page error on allocate; allocate aborted", ex);
            }
            return ret;
        } else {
            throw new IllegalStateException("Not enough space left; allocate aborted");
        }
    }
    private void extendDb(int num) throws IOException {
        // updates free pool
        freePages += num;
        fPage.setInt(FREE_PAGES, freePages);
        
        fPage.setNext(mSize);
        mFile.seek((long) (mSize * fPage.getSize()));
        mFile.writeInt(0); // set Prev

        for (int i = mSize + 1; i < mSize + num; ++i) {
            mFile.writeInt(i); // set Next
            mFile.writeInt(fPage.getFree());
            mFile.seek((long) (i * fPage.getSize()));
            mFile.writeInt(i-1); // set Prev
        }
        mFile.writeInt(0);
        mSize += num;
        fPage.setInt(M_SIZE, mSize);
        fPage.setPrev(mSize - 1);
    }
    public void deallocatePage(int pageId){
        if (pageId > 0 && pageId < mSize) {
            try {
                Page local = readPage(pageId);
                local.setNext(fPage.getNext());
                local.setPrev(fPage.getId());
                local.setInitialFree();
                writePage(local);
                // setting prev
                Page nextLocal = readPage(fPage.getNext());
                nextLocal.setPrev(local.getId());
                writePage(nextLocal);
                
                fPage.setNext(local.getId());
                writePage(fPage);
                ++freePages;
                fPage.setInt(FREE_PAGES, freePages);
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
//                mFile.close();
//                mFile = new RandomAccessFile(fName, "rw");
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
            fPage.setNext(0);
            fPage.setPrev(0);
            fPage.setInitialFree();
            fPage.setInt(FREE_PAGES, 0);
            fPage.setInt(M_SIZE, 1);
            writePage(fPage);
        } catch (IOException except) {
            throw new RuntimeException("Well, error! (creating db)");
        }
    }
    public void openDB(String f) {
        // Need many error checks many many many!!!
        fName = f;
        mSize = 1;
        try {
            mFile = new RandomAccessFile(fName, "rw");
            fPage = readPage(0);
            mSize = fPage.getInt(M_SIZE);
            freePages = fPage.getInt(FREE_PAGES);
        } catch (IOException except) {
            throw new RuntimeException("Well, error! (opening db)");
        }
    }
    public void closeDB() {
        /// Write the control page
        try {
            // neeeeeed to flush !!!!
            writePage(fPage);
            mFile.close();
        } catch (IOException ex) {
            throw new RuntimeException("Well, error! (flushing to disk before exit)");
        }
    }
    public void deleteDB() {
        closeDB();
        File var1 = new File(fName);
        var1.delete();
    }
}

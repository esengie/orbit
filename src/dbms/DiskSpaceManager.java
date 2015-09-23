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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
/**
 *
 * @author Shamil Garifullin <shamil.garifullin at mit.spbau>
 */
public class DiskSpaceManager {

    private static final int MAX_SIZE = 1000000;
    private int mSize = 1;
    private int firstFree = 1;
    private int freePages;
    private RandomAccessFile mFile;
    private String fName;
    private Page fPage;
    private ByteBuffer swap;

    public DiskSpaceManager() {
        fPage = new Page(0);
        swap = ByteBuffer.allocate(fPage.getSize());
    }

    int allocatePage() {
        if (mSize < MAX_SIZE || freePages > 0) {
            if (0 == freePages){
                freePages += 100;
                mSize += 100;
            }
            //// 
            return firstFree;
        } else {
            throw new IllegalStateException("Not enough space left; allocate aborted");
        }
    }

    void deallocatePage(int pageId) {
        if (pageId > 0 && pageId < mSize) {
            ++freePages;

        } else {
            throw new IllegalArgumentException("Invalid deallocate; aborted");
        }
    }

    Page readPage(int pageId) throws IOException {
        if (pageId >= 0 && pageId < mSize) {
            Page p = new Page(pageId);
            mFile.seek((long) (pageId * p.getSize()));
            mFile.read(p.buff.array());
            return p;
        } else {
            throw new IllegalArgumentException("Invalid page number; read aborted");
        }
    }

    void writePage(Page page) throws IOException {
        if (page.getId() >= 0 && page.getId() < mSize) {
            mFile.seek((long) (page.getId() * page.getSize()));
            mFile.write(page.buff.array());
        } else {
            throw new IllegalArgumentException("Invalid page number; write aborted");
        }

    }

    public void closeDB() {
        /// Write the control page
        try {
            // neeeeeed to flush !!!!
            writePage(fPage);
            mFile.close();
        } catch (IOException ex) {
            System.out.println("Well, suck my dick then! (flushing to disk before exit)");;
        }
    }

    public void createDB(String f) {    // first page is reserved
        fName = f;
        try {
            mFile = new RandomAccessFile(fName, "rw");
            fPage.setNext(1);
            writePage(fPage);
            freePages = mSize - 1;
        } catch (IOException except) {
            System.out.println("Well, suck my dick then! (creating db)");
        }
    }

    public void openDB(String f) {
        fName = f;
        try {
            mFile = new RandomAccessFile(fName, "rw");
            fPage = readPage(0);
        } catch (IOException except) {
            System.out.println("Well, suck my dick then! (opening db)");
        }
    }

    public void deleteDB() {
        closeDB();
        File var1 = new File(fName);
        var1.delete();
    }
}

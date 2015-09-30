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

import dbms.FilesAndAccess.Record;
import dbms.BufferManager.BufferManager;
import dbms.DiskSpaceManager.DiskSpaceManager;

/**
 *
 * @author esengie
 */
public class HeapFile {
    MetaPage metad;
    private final int globalFree = 0;
//    private final int myFull;
//    private final int myPartial;
    private final int recordSize;

    DiskSpaceManager  ptrDSM;
    BufferManager     ptrBufM;
    public HeapFile(DiskSpaceManager d, BufferManager b, int rSize) {
        ptrDSM  = d;
        ptrBufM = b;
        recordSize = rSize;
    }
    // if exists
//    HeapFile(MetaPage p){
//        
//    }
    void insertRecord(Record rec){
        
    }
    void deleteRecord(int rid){
        
    }
//    Record get(Record.Rid rid){
//        
//        return r;
//    }
    void destroy(){
        
    }
    
}

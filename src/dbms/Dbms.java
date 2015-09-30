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

import dbms.SettingsAndMeta.RecordStructure;
import dbms.FilesAndAccess.HeapPage;
import dbms.FilesAndAccess.Record;
import dbms.BufferManager.BufferManager;
import dbms.DiskSpaceManager.DiskSpaceManager;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Shamil Garifullin <shamil.garifullin at mit.spbau>
 */
public class Dbms {

    DiskSpaceManager f;
    BufferManager buf;
    RecordStructure struc;
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    Dbms() throws IOException {
        f = new DiskSpaceManager();
        buf = new BufferManager(5);
        buf.setManager(f);
        f.createDB("atari.txt");
        
        struc = new RecordStructure();
        struc.addField("fap", "Int");
        struc.addCharField("lap", 22);
        
        ArrayList<Integer> arr = new ArrayList<>();
        byte[] er; 
        for (Integer i = 0; i < 7; ++i){
            arr.add(f.allocatePage()); // --usage
            Integer temp = arr.get(i);
            String s = new String("fffffffffffffffffffffffffffffffffff" + i.toString());
            er = hexStringToByteArray(s);
            
            HeapPage p = new HeapPage(temp, er.length, buf);   // --usage, it is pinned
            p.create();
            Record rec = new Record(struc);
            rec.buff.put(er);
            p.insertRecord(rec);
        }
        buf.flushAll(); // --usage
        f.closeDB();
   
    }

    public static void main(String[] args) {

        try {
            Dbms df = new Dbms();
            // TODO code application logic here
        } catch (IOException ex) {
            Logger.getLogger(Dbms.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

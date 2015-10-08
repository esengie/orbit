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
package Root;

import SettingsAndMeta.Schema;
import FilesAndAccess.HeapPage;
import FilesAndAccess.Record;
import BufferManager.BufferManager;
import DiskSpaceManager.DiskSpaceManager;
import DiskSpaceManager.Page;
import FilesAndAccess.HeapFile;
//import dbms.DiskSpaceManager.Page;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Map.Entry;
//import java.util.logging.Level;
//import java.util.logging.Logger;

/**
 *
 * @author Shamil Garifullin <shamil.garifullin at mit.spbau>
 */
public class Dbms {

    DiskSpaceManager f;
    BufferManager buf;
    Schema struc;
    HeapFile any;

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

        struc = new Schema();
//        struc.addField("fap", "Int");
        struc.addCharField("lap", 8);

        int met = HeapFile.create(f, buf);
        any = new HeapFile(f, buf, met, struc);
        byte[] er;
        String s = new String("abcdef");
        er = hexStringToByteArray(s);
        int cnt = 0;
        for (Integer i = 0; i < 200; ++i) {
            Record rec = new Record(struc);
            rec.buff.putInt(123123);
            rec.buff.putInt(124);
            any.insertRecord(rec);
        }
        int trop = 5;
        for (Record rec : any) {
            if (trop>200){
                System.out.println(rec.buff.getInt(0));
                System.out.println(rec.buff.getInt(4));
            }
            ++trop;
        }
//        Page pa = buf.getPage(8);
//        pa = buf.getPage(pa.getPrev());
//        System.out.println(pa.getPrev());
//        

        any.destroy();
        buf.flushAll(); // --usage
        f.closeDB();

    }

    public static void main(String[] args) {

        try {
            Dbms df = new Dbms();
            // TODO code application logic here
        } catch (IOException ex) {
//            Logger.getLogger(Dbms.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

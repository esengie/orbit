/*
 * The MIT License
 *
 * Copyright 2015 L.
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

import FilesAndAccess.HeapFile;
import FilesAndAccess.Record;
import Indexes.Index;
import SettingsAndMeta.Schema;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;


/**
 *
 * @author Shamil
 * 
 * Mostly Catalogue and some HeapFile tests
 * 
 * Because everything before was written w/o tests (sorry)
 * 
 * Testing Catalogue because lots of stuff could painfully break
 * 
 */
public class DbmsTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    
    static Schema struc;
    HeapFile any;
    static Dbms db;
    
    ArrayList<String> fields;
    
    public DbmsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        db = new Dbms();
        struc = new Schema();
        struc.createField("fap", "int");
        struc.createCharField("mishap", 12);
        struc.createField("lap", "Int");
        db.create("atari.txt");
        db.cat.createTable("Donkey", struc);
        db.disk.closeDB();
    }
    
    @AfterClass
    public static void tearDownClass() {
//        db.open("atari.txt");
//        db.disk.deleteDB();
    }
    
    @Before
    public void setUp() {
        db.open("atari.txt");
        any = db.cat.getTable("Donkey");
        fields = new ArrayList<>();
        fields.add("fap");
    }
    
    @After
    public void tearDown() {
        db.cat.dropTable("Donkey");
        db.cat.createTable("Donkey", struc);
        db.buf.flushAll();
        db.disk.closeDB();
    }
    
    /**
     * Test of create heapfile iterate and remove.
     */
//    @Test
//    public void testCreate() {
//        System.out.println("create, iterate, remove");
//        for (Integer i = 0; i < 1000; ++i) {
//            Record rec = new Record(struc);
//            rec.putInt("fap", i);
//            rec.putInt("lap", 0);
//            rec.putString("mishap", "asdas");
//            any.insertRecord(rec);
//        }
//        Iterator<Record> it = any.iterator();
//        while(it.hasNext()) {
//            Record rec = it.next();
//            int vv = rec.getInt("fap");
//            if (vv % 5 == 0){
//                it.remove();
//            }
//        }
//        Set set = new HashSet();
//        for (Record rec : any){
//            set.add(rec.getInt("fap"));
//            assertTrue(rec.getInt("fap")%5 != 0);
//        }
//        assertEquals(800, set.size());
//    }

    @Test
    public void testSameBases() {
//        expectedEx.expect(IllegalArgumentException.class);
//        expectedEx.expectMessage("Same name index already exists!");
        db.cat.createTable("asd", struc);
        db.cat.createTable("asds", struc);
        db.cat.dropTable("asd");
        db.cat.createTable("asd", struc);
        db.cat.dropTable("asds");
        for (Record r : db.cat.metaPages){
            System.out.println(r.getString("table_name"));
        }
        
    }
    @Test
    public void testSameNameIndices() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Same name index already exists!");
        fields.add("lap");
        db.cat.createIndex("Donkey_aaaaa", "Donkey", fields, Index.IndexType.BTREE);
        db.cat.createIndex("Donkey_aaaaa", "Donkey", fields, Index.IndexType.BTREE);
        
    }
    /**
     * Test of create heapfile iterate and remove.
     */
    @Test
    public void testSameFuncIndices() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Same function index already exists! Named: Donkey_i");
        db.cat.createIndex("Donkey_i", "Donkey", fields, Index.IndexType.BTREE); 
        for (Record r : db.cat.indices){
            String tst = r.getString("index_name");
            System.out.println(tst);
        }
        db.cat.createIndex("Donkey_g", "Donkey", fields, Index.IndexType.BTREE);
    }
    
    @Test
    public void testDeleteIndices() {
        db.cat.createIndex("Donkey_", "Donkey", fields, Index.IndexType.BTREE);
        db.cat.dropIndex("Donkey_");
        db.cat.createIndex("Donkey_", "Donkey", fields, Index.IndexType.BTREE);
    }
}

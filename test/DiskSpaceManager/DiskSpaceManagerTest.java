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
 * @author Shamil Garifullin <shamil.garifullin at mit.spbau>
 */
public class DiskSpaceManagerTest {
    
    public DiskSpaceManagerTest() {
    }
    
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    
    static DiskSpaceManager db;
    static final String name = "disk_man_test";
    
    @BeforeClass
    public static void setUpClass() {
        db = new DiskSpaceManager();
        db.createDB(name);
        db.closeDB();
    }
    
    @AfterClass
    public static void tearDownClass() {
        db.openDB(name);        
        db.deleteDB();
    }
    
    @Before
    public void setUp() {
        db.openDB(name);        
    }
    
    @After
    public void tearDown() {
        db.closeDB();
    }
    
    @Test
    public void testCreate() {
        db.allocatePage();
        db.allocatePage();
        db.allocatePage();        
        db.deleteDB();
        db.createDB(name);
        for (int i = 0; i < 5; ++i){
            db.deleteDB();
            db.createDB(name);
            int k = db.allocatePage();
            assertEquals(1, k);
            k = db.allocatePage();
            assertEquals(2, k);
            k = db.allocatePage();
            assertEquals(3, k);
            // Testing out deleted
            Page kk = db.readPage(2);
            kk.setDeleted();
            db.writePage(kk);
            kk = db.readPage(1);
            kk.setDeleted();
            db.writePage(kk);
            k = db.allocatePage();
            assertEquals(1, k);
        }
    }
    
}

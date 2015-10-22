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
package SettingsAndMeta;

import BufferManager.BufferManager;
import DiskSpaceManager.DiskSpaceManager;
import FilesAndAccess.HeapFile;
import FilesAndAccess.Record;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author esengie
 */
public class Catalogue extends GlobalConsts {
    private HeapFile schemas;
    private HeapFile metaPages;
    private final Schema s_schema;
    private final Schema m_schema;
    private final DiskSpaceManager disk;
    private final BufferManager buf;
    private final String tableName = "table_name";
    private final String attrName = "attr_name";
    private final String fieldType = "type";
    private final String fieldPos = "position";
    private final String metaName = "meta_page";
    
    public Catalogue(DiskSpaceManager d, BufferManager b){
        disk = d;
        buf = b;
        s_schema = new Schema();
        s_schema.addCharField(attrName, MAX_NAME_SIZE);
        s_schema.addCharField(tableName, MAX_NAME_SIZE);
        s_schema.addCharField(fieldType, 15); // varchar, int, double
        s_schema.addField(fieldPos, "int"); // where the attr in record located
        m_schema = new Schema();
        m_schema.addCharField(tableName, MAX_NAME_SIZE);
        m_schema.addField(metaName, "int");
    }
    public void create(){
        HeapFile.create(disk, buf); // always 1
        HeapFile.create(disk, buf); // always 4 (we need 3 pages for a HeapFile)
        load();
    }
    public void load(){
        schemas = new HeapFile(disk, buf, META_SCHEMA_PAGE, s_schema);
        metaPages = new HeapFile(disk, buf, META_META_PAGE, m_schema);
    }
    public HeapFile createTable(String name, Schema s){
        for (Record rec : metaPages){
            String tst = rec.getString(tableName);
            if (tst.equals(name)){
                throw new IllegalArgumentException("Table already exists!");
            }
        }
        int met = HeapFile.create(disk, buf);
        for (String item : s){
            Record rec = new Record(s_schema);
            rec.putString(attrName, item);
            rec.putString(tableName, name);
            rec.putString(fieldType, s.getType(item));
            rec.putInt(fieldPos, s.getPos(item));
            schemas.insertRecord(rec);
        }
        Record rec = new Record(m_schema);
        rec.putString(tableName, name);
        rec.putInt(metaName, met);
        metaPages.insertRecord(rec);
        
        return new HeapFile(disk, buf, met, s);
    }
    public HeapFile getTable(String name){
        Schema ret_s = new Schema();
        int met = 0;
        for (Record item : schemas){
            if (item.getString(tableName).equals(name)){
                ret_s.addField(item.getString(attrName), item.getString(fieldType));
            }
        }
        for (Record item : metaPages){
            if (item.getString(tableName).equals(name)){
                met = item.getInt(metaName);
            }
        }
        if (met == 0){
            throw new IllegalAccessError("No meta page found in the Catalogue!");
        }
        return new HeapFile(disk, buf, met, ret_s);
    }
    public void dropTable(String name){
        HeapFile f = getTable(name);
        f.destroy();
        for (Record rec : metaPages){
            String tst = rec.getString(tableName);
            if (tst.equals(name)){
                metaPages.deleteRecord(rec.getRid());
                break;
            }
        }
        List<Record.Rid> rids = new ArrayList<>();
        for (Record rec : schemas){
            String tst = rec.getString(tableName);
            if (tst.equals(name)){
                rids.add(rec.getRid());
            }
        }
        for (Record.Rid rid : rids){
            schemas.deleteRecord(rid);
        }
    }
}

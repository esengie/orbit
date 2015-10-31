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
import Indexes.BTreeIndex;
import Indexes.HashIndex;
import Indexes.Index;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author esengie
 */
public class Catalogue extends GlobalConsts {

    private HeapFile attributes;
    public HeapFile metaPages;
    public HeapFile indices;
    private final Schema s_schema;
    private final Schema m_schema;
    private final Schema i_schema;
    private final DiskSpaceManager disk;
    private final BufferManager buf;
    private final String c_tableName = "table_name";
    private final String c_indexName = "index_name";
    private final String c_attrName = "attr_name";
    private final String c_indexType = "type";
    private final String c_fieldType = "type";
    private final String c_fieldPos = "position";
    private final String c_metaName = "meta_page";

    public Catalogue(DiskSpaceManager d, BufferManager b) {
        disk = d;
        buf = b;
        s_schema = new Schema();
        s_schema.createCharField(c_attrName, MAX_NAME_SIZE);
        s_schema.createCharField(c_tableName, MAX_NAME_SIZE);
        s_schema.createCharField(c_fieldType, 15); // varchar, int, double
        s_schema.createField(c_fieldPos, "int"); // where the attr in record located
        m_schema = new Schema();
        m_schema.createCharField(c_tableName, MAX_NAME_SIZE);
        m_schema.createField(c_metaName, "int");
        i_schema = new Schema();
        i_schema.createCharField(c_indexName, MAX_NAME_SIZE);
        i_schema.createCharField(c_tableName, MAX_NAME_SIZE);
        i_schema.createCharField(c_attrName, MAX_NAME_SIZE * 5);
        i_schema.createCharField(c_indexType, MAX_NAME_SIZE);
    }

    public void create() {
        HeapFile.create(disk, buf); // always 1
        HeapFile.create(disk, buf); // always 4 (we need 3 pages for a HeapFile)
        HeapFile.create(disk, buf); // always 7
        load();
    }

    public void load() {
        attributes = new HeapFile(disk, buf, META_SCHEMA_PAGE, s_schema);
        metaPages = new HeapFile(disk, buf, META_META_PAGE, m_schema);
        indices = new HeapFile(disk, buf, META_INDEX_PAGE, i_schema);
    }

    public void createIndex(String indexName, String tableName, ArrayList<String> fields, Index.IndexType type) {
        // Check if schema has such fields
        HeapFile onTable = getTable(tableName);
        Schema ss = onTable.getSchema();
        for (int i = 0; i < fields.size(); ++i) {
            // Just cause it throws if no such field
            ss.getType(fields.get(i));
        }
        // Such is life, concat to one string
        String field = Index.fieldsToString(fields);
        String typo = Index.typeToString(type);

        // Check uniqueness and user dumbness
        for (Record rec : indices) {
            String i_name = rec.getString(c_indexName);
            String att = rec.getString(c_attrName);
            String t_name = rec.getString(c_tableName);
            String ty = rec.getString(c_indexType);
            if (i_name.equals(indexName)){
                throw new IllegalArgumentException("Same name index already exists!");
            }
            if (att.equals(field) && ty.equals(typo) && t_name.equals(tableName)) {
                throw new IllegalArgumentException("Same function index already exists! Named: "
                        + rec.getString(c_indexName));
            }
        }
        // Oh hey, a useful line!
        HeapFile self = createTable(indexName, new Schema());
        
        // ..more of maintenance
        Record rec = new Record(i_schema);
        rec.putString(c_indexName, indexName);
        rec.putString(c_tableName, tableName);
        rec.putString(c_attrName, field);
        rec.putString(c_indexType, typo);
        indices.insertRecord(rec);
        
        // Another two usefuls!
        Index ind = createHelper(type, self, fields, onTable);
        ind.create();
    }
    private Index createHelper(Index.IndexType t, HeapFile self, ArrayList<String> fields, HeapFile on){
        switch(t){
            case BTREE: 
                return new BTreeIndex(self, fields, on);
            default:
                return new HashIndex(self, fields, on);
        }
    }
    public Index getIndex(String indexName){
        HeapFile self = getTable(indexName);
        Index.IndexType type = Index.IndexType.BTREE;
        ArrayList<String> fields = new ArrayList<>();
        String tableName = "";
        for (Record item : indices) {
            if (item.getString(c_tableName).equals(indexName)) {
                type = Index.toType(item.getString(c_indexType));
                fields = Index.toFields(item.getString(c_attrName));
                tableName = item.getString(c_tableName);
                break;
            }
        }
        if (tableName.equals("")){
            throw new IllegalArgumentException("No such Index! " + indexName);
        }
        return createHelper(type, self, fields, getTable(tableName));
    }
    public void dropIndex(String indexName){
        dropTable(indexName);
        for (Record rec : indices) {
            String tst = rec.getString(c_indexName);
            if (tst.equals(indexName)) {
                indices.deleteRecord(rec.getRid());
                break;
            }
        }
    }
    
    public HeapFile createTable(String name, Schema s) {
        for (Record rec : metaPages) {
            String tst = rec.getString(c_tableName);
            if (tst.equals(name)) {
                throw new IllegalArgumentException("Table already exists!");
            }
        }
        int met = HeapFile.create(disk, buf);
        for (String item : s) {
            Record rec = new Record(s_schema);
            rec.putString(c_attrName, item);
            rec.putString(c_tableName, name);
            rec.putString(c_fieldType, s.getType(item));
            rec.putInt(c_fieldPos, s.getPos(item));
            attributes.insertRecord(rec);
        }
        Record rec = new Record(m_schema);
        rec.putString(c_tableName, name);
        rec.putInt(c_metaName, met);
        metaPages.insertRecord(rec);

        return new HeapFile(disk, buf, met, s);
    }
    public HeapFile getTable(String name) {
        Schema ret_s = new Schema();
        int met = 0;
        for (Record item : attributes) {
            String tst = item.getString(c_tableName);
            if (tst.equals(name)) {
                ret_s.addField(item.getString(c_attrName), item.getString(c_fieldType), item.getInt(c_fieldPos));
            }
        }
        for (Record item : metaPages) {
            String tst = item.getString(c_tableName);
            if (tst.equals(name)) {
                met = item.getInt(c_metaName);
            }
        }
        if (met == 0) {
            throw new IllegalAccessError("No meta page found in the Catalogue! For " + name);
        }
        return new HeapFile(disk, buf, met, ret_s);
    }

    public void dropTable(String name) {
        HeapFile f = getTable(name);
        f.destroy();
        for (Record rec : metaPages) {
            String tst = rec.getString(c_tableName);
            if (tst.equals(name)) {
                metaPages.deleteRecord(rec.getRid());
                break;
            }
        }
        List<Record.Rid> rids = new ArrayList<>();
        for (Record rec : attributes) {
            String tst = rec.getString(c_tableName);
            if (tst.equals(name)) {
                rids.add(rec.getRid());
            }
        }
        for (Record.Rid rid : rids) {
            attributes.deleteRecord(rid);
        }
        List<String> ind = new ArrayList<>();
        for (Record rec : indices){
            String tst = rec.getString(c_tableName);
            if (tst.equals(name)){
                ind.add(rec.getString(c_indexName));
            }
        }
        for (String nm : ind) {
            dropIndex(nm);
        }
    }
}

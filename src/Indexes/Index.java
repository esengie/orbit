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
package Indexes;

import FilesAndAccess.HeapFile;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Shamil Garifullin <shamil.garifullin at mit.spbau>
 */
public class Index {
    public enum IndexType {
        HASH, BTREE
    }
    
    public Index(HeapFile myself, ArrayList<String> fields, HeapFile on){
        //do stuff
    }
    
    public void create(){
        // actually do stuff
    }
    
    public static String fieldsToString(ArrayList<String> fields){
        if (fields.size() > 5){
            throw new IllegalArgumentException("If you want combined indices on more than 5 fields, pay me!");
        }
        String field = fields.get(0);
        if (fields.size() > 1){
            for (int i = 1; i < fields.size(); ++i){
                field += "," + fields.get(i);
            }
        }
        return field;
    }
    public static ArrayList<String> toFields(String field){
        ArrayList<String> fields = new ArrayList<>(Arrays.asList(field.split(",")));
        return fields;
    }
    public static String typeToString(IndexType t){
        switch (t){
            case HASH:
                return "hash";
            default:
                return "btree";
        }
    }
    public static IndexType toType(String t){
        switch (t){
            case "hash":
                return IndexType.HASH;
            default:
                return IndexType.BTREE;
        }
    }
}

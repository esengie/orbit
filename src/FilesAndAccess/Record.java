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
package FilesAndAccess;

import SettingsAndMeta.Schema;
import java.nio.ByteBuffer;

/**
 *
 * @author esengie
 */
public class Record {
    public class Rid{
        public int pid;
        public int sid;
    }
    public ByteBuffer buff;
    private final Rid rid;
    private final Schema schema;
    
    public Record(Schema str){
        schema = str;
        buff = ByteBuffer.allocate(str.getRecordSize());
        rid = new Rid();
    }
    public void setRid(int pid, int sid){
        rid.pid = pid;
        rid.sid = sid;
    }
    public Rid getRid(){
        return rid;
    }
    public String getString(String name){
        int p  = schema.getPos(name);
        int sz = schema.getSize(name);
        char ret[] = new char[sz];
        buff.position(p);
        int i = 0;
        for (i = 0; i < sz; ++i){
            ret[i] = buff.getChar();
            if (ret[i] == 0) break;
        }
        return new String(ret, 0, i);
    }
    public void putString(String name, String s){
        int p  = schema.getPos(name);
        int sz = schema.getSize(name);
        char ret[] = s.toCharArray();
        buff.position(p);
        for (int i = 0; i < s.length(); ++i){
            buff.putChar(ret[i]);
        }
    }
    public int getInt(String name){
        int p  = schema.getPos(name);
        return buff.getInt(p);
    }
    public void putInt(String name, int v){
        int p  = schema.getPos(name);
        buff.putInt(p, v);
    }
    public double getDouble(String name){
        int p  = schema.getPos(name);
        return buff.getDouble(p);
    }
    public void putDouble(String name, double v){
        int p  = schema.getPos(name);
        buff.putDouble(p, v);
    }
}



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
package SettingsAndMeta;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Shamil Garifullin <shamil.garifullin at mit.spbau>
 */
public class Schema extends GlobalConsts implements Iterable<String> {

    private final Map<String, Map.Entry<String, Integer>> fields;
    private int recordSize;

    public Schema() {
        fields = new HashMap<>();
    }

    @Override
    public Iterator<String> iterator() {
        return fields.keySet().iterator();
    }
 
    public int getRecordSize() {
        return recordSize;
    }
    public void addField(String name, String s){
        recordSize += addField(name, s, recordSize);
    }
    public int addField(String name, String s, Integer pos) {
        if (s.length() < 7) {
            switch (s.toLowerCase()) {
                case "int":
                    fields.put(name.toLowerCase(),
                            new AbstractMap.SimpleEntry<>("int____", pos));
                    return INT_SIZE;
                case "double":
                    fields.put(name.toLowerCase(),
                            new AbstractMap.SimpleEntry<>("double_", pos));
                    return DOUBLE_SIZE;
                default:
                    throw new IllegalArgumentException("Unknown type: need either INT, DOUBLE or VARCHAR");
            }
        } else {
            if (!"varchar".equals(s.substring(0, 7))){
                throw new IllegalArgumentException("Unknown type: need either INT, DOUBLE or VARCHAR");
            }
            int maxSize = 0;
            try {
                maxSize = Integer.parseInt(s.substring(7));
            } catch (Exception e){
                throw new IllegalArgumentException("Incorrect length: VARCHAR");
            }
            if (maxSize > 128) {
                throw new IllegalArgumentException("Your varchar is too big, max = 128");
            }
            fields.put(name.toLowerCase(),
                new AbstractMap.SimpleEntry<>("varchar" + maxSize, recordSize));
            return 2 * maxSize;
        }
    }

    public void addCharField(String name, int maxSize) {
        if (maxSize > 128) {
            throw new IllegalArgumentException("Your varchar is too big, max = 128");
        }
        fields.put(name.toLowerCase(),
                new AbstractMap.SimpleEntry<>("varchar" + maxSize, recordSize));
        recordSize += 2 * maxSize;
    }

    public int getPos(String name) {
        if (fields.containsKey(name)) {
            return fields.get(name).getValue();
        } else {
            throw new IllegalArgumentException("No such field in a record");
        }
    }
    public String getType(String name){
        if (fields.containsKey(name)) {
            return fields.get(name).getKey();
        } else {
            throw new IllegalArgumentException("No such field in a record");
        }
    }
    public int getSize(String name) {
        if (fields.containsKey(name)) {
            String s = fields.get(name).getKey();
            String temp = s.substring(0, 7);
            switch (temp) {
                case "int____":
                    return INT_SIZE;
                case "double__":
                    return DOUBLE_SIZE;
                default:
                    //case "varchar": 
                    return Integer.parseInt(s.substring(7));
            }
        } else {
            throw new IllegalArgumentException("No such field in a record");
        }
    }
}

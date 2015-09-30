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
package dbms.SettingsAndMeta;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Shamil Garifullin <shamil.garifullin at mit.spbau>
 */
public class RecordStructure {

    private Map<String, Integer> fields;
    private int recordSize;

    public RecordStructure() {
        fields = new HashMap<>();
    }
    
    public int getRecordSize(){
        return recordSize;
    }
    public void addField(String name, String s) {
        switch (s.toLowerCase()) {
            case "int":
                fields.put(name.toLowerCase(), 4);
                recordSize += 4;
                break;
            case "double":
                fields.put(name.toLowerCase(), 8);
                recordSize += 8;
                break;
            default:
                throw new IllegalArgumentException("Unknown type: need either INT, DOUBLE or VARCHAR");
        }
    }
    public void addCharField(String name, int maxSize) {
        if (maxSize > 128) {
            throw new IllegalArgumentException("Your varchar is too big, max = 128");
        }
        fields.put(name.toLowerCase(), maxSize);
        recordSize += maxSize;
    }

}

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

import java.io.IOException;
import static java.lang.Math.max;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Shamil Garifullin <shamil.garifullin at mit.spbau>
 */
public class BufferManager {   ///singleton

    private int nFrames;
    private Map<Integer, Boolean> dirty;
    private Map<Integer, Integer> pinCount;
    private Map<Integer, Page> pageFrame;
    private int nPinned;
    private LRUCache<Integer, Page> lru;
    DiskSpaceManager diskManager;

    BufferManager(int n) {
        nFrames = n;
        nPinned = max(nFrames / 20, 5);
        dirty = new HashMap<>(nFrames);
        pinCount = new HashMap<>(nPinned);
        pageFrame = new HashMap<>(nPinned);
        lru = new LRUCache<>(max(nFrames - nPinned, 5)); // Costyl
    }

    BufferManager() {
        this(200);
    }

    void setManager(DiskSpaceManager m) {
        diskManager = m;
    }

    private void pin(int pageId) {
        if (pinCount.containsKey(pageId)) {
            pinCount.put(pageId, pinCount.get(pageId) + 1);
            return;
        }
        pinCount.put(pageId, 1);
        if (pageFrame.size() > nPinned) {
            throw new RuntimeException("Unexpected to have more than 5% pinned pages");
        }
        pageFrame.put(pageId, lru.get(pageId));
        lru.remove(pageId);
    }

    public void unpin(int pageId) throws IOException {
        if (pinCount.containsKey(pageId)) {
            Integer temp = pinCount.get(pageId);
            if (temp == 1) {
                if (lru.size() > 0) {
                    Entry<Integer, Page> entry = lru.entrySet().iterator().next();
                    if (lru.size() >= lru.capacity() - 1
                            && dirty.get(entry.getKey())) {
                        diskManager.writePage(entry.getValue());
                        dirty.remove(entry.getKey());
                    }
                }
                pinCount.remove(pageId);
                lru.put(pageId, pageFrame.get(pageId));
                pageFrame.remove(pageId);
                return;
            }
            pinCount.put(pageId, temp - 1);
        }
    }

    public void setDirty(int pageId) {
        if (pageFrame.containsKey(pageId)) {
            dirty.put(pageId, Boolean.TRUE);
        }
    }

    protected void flushAll() {
        try {
            for (Entry<Integer, Boolean> elem : dirty.entrySet()) {
                diskManager.writePage(lru.get(elem.getKey()));
            }
        } catch (IOException ex) {
            System.err.println("Well, suck my dick then! (opening db)");
        }
    }

    public Page getPage(int pageId) throws IOException {
        if (lru.containsKey(pageId)) {
            pageFrame.put(pageId, lru.get(pageId));
            lru.remove(pageId);
        }
        if (pageFrame.containsKey(pageId)) {
            pin(pageId);
            return pageFrame.get(pageId);
        } else {
            pin(pageId);
            Page temp = diskManager.readPage(pageId);
            pageFrame.put(pageId, temp);
            return temp;
        }
    }
}

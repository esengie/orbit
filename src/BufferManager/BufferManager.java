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
package BufferManager;

import DiskSpaceManager.Page;
import DiskSpaceManager.DiskSpaceManager;
import SettingsAndMeta.GlobalConsts;
import static java.lang.Math.max;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Shamil Garifullin <shamil.garifullin at mit.spbau>
 */
public class BufferManager extends GlobalConsts {   ///singleton

    private int nFrames;
    private Map<Integer, Boolean> dirty;
    private Map<Integer, Integer> pinCount;
    private Map<Integer, Page> pageFrame;
    private int nPinned;
    private LRUCache<Integer, Page> lru;
    DiskSpaceManager diskManager;

    public BufferManager(int n) {
        nFrames = n;
        nPinned = max(nFrames / BUFF_DEFAULT_PINNED_FRACTIONS, BUFF_DEFAULT_MIN_PAGES);
        dirty = new HashMap<>(nFrames);
        pinCount = new HashMap<>(nPinned);
        pageFrame = new HashMap<>(nPinned);
        lru = new LRUCache<>(max(nFrames - nPinned, BUFF_DEFAULT_MIN_PAGES)); // Costyl
    }

    public BufferManager() {
        this(BUFF_DEFAULT_PAGES);
    }

    public void setManager(DiskSpaceManager m) {
        diskManager = m;
    }

    private void pin(int pageId) {
        if (pinCount.containsKey(pageId)) {
            pinCount.put(pageId, pinCount.get(pageId) + 1);
            throw new IllegalStateException("How in the hell did i get pincount more than 1? "
                + String.valueOf(pageId));
//            return;
        }
        
        pinCount.put(pageId, 1);
        if (pageFrame.size() > nPinned) {
            throw new RuntimeException("Unexpected to have more than 5% pinned pages");
        }
        pageFrame.put(pageId, lru.get(pageId));
        lru.remove(pageId);
    }

    public void unpin(int pageId) {
        if (pinCount.containsKey(pageId)) {
            Integer temp = pinCount.get(pageId);
            if (temp == 1) {
                if (lru.size() > 0) {
                    Entry<Integer, Page> entry = lru.entrySet().iterator().next();
                    if (lru.removeEldestEntry(entry)
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
            throw new IllegalStateException("We're single threaded so no double pinning");
        }
    }

    public void setDirty(int pageId) {
        if (pageFrame.containsKey(pageId)) {
            dirty.put(pageId, Boolean.TRUE);
        }
    }

    public void flushAll() {
        Page p = null;
        int i = -1;
        try {
            for (Entry<Integer, Boolean> elem : dirty.entrySet()) {
                i = elem.getKey();
                p = lru.get(elem.getKey());
                if (p == null){
                    throw new RuntimeException("Well, error! (flushing buffer)");
                }
                diskManager.writePage(p);
            }
        } catch (RuntimeException ex) {
            throw new RuntimeException("Flushing buffer error, no page in lru with number "
                    + String.valueOf(i) + " (is it pinned?)" + String.valueOf(pageFrame.containsKey(i)));
        }
    }

    public Page getPage(int pageId) {
        if (!lru.containsKey(pageId) && !pageFrame.containsKey(pageId)) {
            try {
                Page temp = diskManager.readPage(pageId);
                pinCount.put(pageId, 1);
                pageFrame.put(pageId, temp);
            } catch (RuntimeException ex) {
                throw new RuntimeException("Well, error! (getting page) " + ex.getMessage());
            }
        } else {
            pin(pageId);
        }
        return pageFrame.get(pageId);
    }
}

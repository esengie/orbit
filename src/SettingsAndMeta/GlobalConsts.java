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

/**
 *
 * @author Shamil Garifullin <shamil.garifullin at mit.spbau>
 */
public class GlobalConsts {
    protected static final int INT_SIZE = 4;
    protected static final int DOUBLE_SIZE = 8;
    // Page consts
    protected static final int PAGE_PREV = 0;
    protected static final int PAGE_NEXT = 4;
    protected static final int PAGE_SIZE = 4096;
    protected static final int PAGE_DELETED = 8;
    protected static final int PAGE_FREE = 9;
    protected static final int page_offset = PAGE_FREE + 4;
    // Disk Space manager consts
    protected static final int DBMS_FILE_MAX_SIZE = 1000000;
    protected static final int EXTEND_DB = 10;
    // Buffer Manager consts
    protected static final int BUFF_DEFAULT_PAGES = 200;
    protected static final int BUFF_DEFAULT_MIN_PAGES = 5;
    protected static final int BUFF_DEFAULT_PINNED_FRACTIONS = 20;
    // Metadata consts
    protected static final int META_PAGE_RECORD_SIZE = 4;
    protected static final int META_HALF_FULL_LOC = 0;
    protected static final int META_FULL_LOC = 1;
    protected static final int META_TOTAL_RECS_LOC = 2;
}

/*
 * Copyright (c) 2011 Sergey Prilukin
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jstreamserver.utils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for rendering requests
 *
 * @author Sergey Prilukin
 */
public final class HttpUtils {

    public static final String DEFAULT_ENCODING = "UTF-8";

    public static final String CONTENT_TYPE_HEADER = "Content-Type";

    public static final String CONTENT_ENCODING_HEADER = "Content-Encoding";

    public static final String CONTENT_RANGE_HEADER = "Content-Range";

    public static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";

    public static final String GZIP_ENCODING = "gzip";

    public static final String DEFAULT_TEXT_CONTENT_TYPE = "text/html; charset=" + DEFAULT_ENCODING;

    public static final String CONTENT_RANGE_FORMAT = "bytes %s-%s/%s";

    public static final String CONTENT_DISPOSITION_FORMAT = "attachment; filename=%s";
}

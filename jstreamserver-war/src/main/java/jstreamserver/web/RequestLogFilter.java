/*
 * Copyright (c) 2012 by Sergey Prilukin
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

package jstreamserver.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO: description
 *
 * @author Sergey Prilukin
 */
public class RequestLogFilter implements Filter {

    public static final String REMOTE_HOST_REGEXP = "^/([^\\:]+)\\:[\\d]+$";

    private static final Log log = LogFactory.getLog(RequestLogFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        /* nothing to do */
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } finally {
            logRequest((HttpServletRequest)request, (HttpServletResponse)response);
        }
    }

    private void logRequest(HttpServletRequest request, HttpServletResponse response) {
        if (log.isDebugEnabled()) {
            //--Log request
            log.debug(String.format("%s %s %s", request.getMethod(), request.getRequestURI(), request.getProtocol()));

            //request headers
            Enumeration enumeration = request.getHeaderNames();
            while (enumeration.hasMoreElements()) {
                String requestName = (String)enumeration.nextElement();
                log.debug(String.format("%s: %s", requestName, request.getHeader(requestName)));
            }

            log.debug("\r\n");
        } else if (log.isInfoEnabled()) {
            //Short format

            String remoteHost = request.getRemoteAddr();
            String remoteAddress = remoteHost;
            Matcher matcher = Pattern.compile(REMOTE_HOST_REGEXP).matcher(remoteHost);
            if (matcher.find()) {
                remoteAddress = matcher.group(1);
            }

            String method = request.getMethod();
            String path = request.getRequestURI();
            String userAgent = request.getHeader("User-Agent");

            log.info(String.format("%s [%s] [%s] [%s]", remoteAddress, method, path, userAgent));
        }
    }

    @Override
    public void destroy() {
        /* nothing to do */
    }
}

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

package jstreamserver.ftp;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.User;

import java.util.List;

/**
 * Basic implementation of {@link User}
 *
 * @author Sergey Prilukin
 */
public class FtpUser implements User {
    private String name;
    private String password;
    private List<Authority> authorities;
    private int maxIdleTime;
    private boolean enabled;
    private String homeDirectory;

    public FtpUser(String name, String password, List<Authority> authorities, int maxIdleTime, boolean enabled, String homeDirectory) {
        this.name = name;
        this.password = password;
        this.authorities = authorities;
        this.maxIdleTime = maxIdleTime;
        this.enabled = enabled;
        this.homeDirectory = homeDirectory;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public List<Authority> getAuthorities() {
        return authorities;
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public String getHomeDirectory() {
        return homeDirectory;
    }

    @Override
    public List<Authority> getAuthorities(Class<? extends Authority> aClass) {
        return null;
    }

    @Override
    public AuthorizationRequest authorize(AuthorizationRequest authorizationRequest) {
        return authorizationRequest;
    }
}

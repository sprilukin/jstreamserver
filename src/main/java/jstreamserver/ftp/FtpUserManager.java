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

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;

/**
 * Implementation of {@link UserManager}
 *
 * @author Sergey Prilukin
 */
public class FtpUserManager implements UserManager {

    @Override
    public User getUserByName(String s) throws FtpException {
        return new FtpUser(s, null, null, 0, true, "/");
    }

    @Override
    public String[] getAllUserNames() throws FtpException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(String s) throws FtpException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(User user) throws FtpException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean doesExist(String s) throws FtpException {
        throw new UnsupportedOperationException();
    }

    @Override
    public User authenticate(Authentication authentication) throws AuthenticationFailedException {

        if (authentication instanceof UsernamePasswordAuthentication) {
            UsernamePasswordAuthentication upa = (UsernamePasswordAuthentication) authentication;
            return new FtpUser(upa.getUsername(), upa.getPassword(), null, 0, true, "/");
        } else if (authentication instanceof AnonymousAuthentication) {
            return new FtpUser("anonymous", null, null, 0, true, "/");
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String getAdminName() throws FtpException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAdmin(String s) throws FtpException {
        throw new UnsupportedOperationException();
    }
}

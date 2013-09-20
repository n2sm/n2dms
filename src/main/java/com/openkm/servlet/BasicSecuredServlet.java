package com.openkm.servlet;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import com.openkm.module.jcr.JcrRepositoryModule;
import com.openkm.module.jcr.stuff.apache.BasicCredentialsProvider;
import com.openkm.module.jcr.stuff.apache.CredentialsProvider;

public class BasicSecuredServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private CredentialsProvider cp = new BasicCredentialsProvider(null);

    /**
     * Get JCR session
     */
    public synchronized Session getSession(final HttpServletRequest request)
            throws LoginException, javax.jcr.RepositoryException,
            ServletException {
        final Credentials creds = cp.getCredentials(request);
        final Repository rep = JcrRepositoryModule.getRepository();

        if (creds == null) {
            return rep.login();
        } else {
            return rep.login(creds);
        }
    }

    /**
     * Get JCR session
     */
    public synchronized Session getSession(final String user,
            final String password) throws LoginException,
            javax.jcr.RepositoryException, ServletException {
        final Credentials creds = new SimpleCredentials(user,
                password.toCharArray());
        final Repository rep = JcrRepositoryModule.getRepository();
        return rep.login(creds);
    }
}

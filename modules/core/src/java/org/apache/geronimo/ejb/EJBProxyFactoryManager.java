/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.ejb;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import javax.ejb.EJBException;

import org.apache.geronimo.common.AbstractComponent;
import org.apache.geronimo.ejb.container.EJBPlugins;

/**
 *
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/10 20:51:54 $
 */
public class EJBProxyFactoryManager extends AbstractComponent {
    private Map proxies = new HashMap();
    private ThreadLocal threadEJBProxyFactory = new ThreadLocal();

    public void create() throws Exception {
        log.debug("Creating EJBProxyFactoryManager: ejbName=" + EJBPlugins.getEJBMetadata(getContainer()).getName());
        super.create();
        for (Iterator iterator = proxies.keySet().iterator(); iterator.hasNext();) {
            String proxyName = (String) iterator.next();
            EJBProxyFactory ejbProxyFactory = getEJBProxyFactory(proxyName);
            // @todo uncomment this when the ejb proxy factories are rewriten to be full components
            //ejbProxyFactory.setContainer(getContainer());
            log.debug("Creating EJBProxyFactory: proxyName=" + proxyName + " ejbName=" + EJBPlugins.getEJBMetadata(getContainer()).getName());
            ejbProxyFactory.create();
        }
    }

    public void start() throws Exception {
        log.debug("Starting EJBProxyFactoryManager: ejbName=" + EJBPlugins.getEJBMetadata(getContainer()).getName());
        super.start();
        for (Iterator iterator = proxies.keySet().iterator(); iterator.hasNext();) {
            String proxyName = (String) iterator.next();
            EJBProxyFactory ejbProxyFactory = getEJBProxyFactory(proxyName);
            log.debug("Starting EJBProxyFactory: proxyName=" + proxyName + " ejbName=" + EJBPlugins.getEJBMetadata(getContainer()).getName());
            ejbProxyFactory.start();
        }
    }

    public void stop() {
        log.debug("Stopping EJBProxyFactoryManager: ejbName=" + EJBPlugins.getEJBMetadata(getContainer()).getName());
        super.stop();
        for (Iterator iterator = proxies.keySet().iterator(); iterator.hasNext();) {
            String proxyName = (String) iterator.next();
            EJBProxyFactory ejbProxyFactory = getEJBProxyFactory(proxyName);
            log.debug("Stopping EJBProxyFactory: proxyName=" + proxyName + " ejbName=" + EJBPlugins.getEJBMetadata(getContainer()).getName());
            ejbProxyFactory.stop();
        }
    }

    public void destroy() {
        log.debug("Destroying EJBProxyFactoryManager: ejbName=" + EJBPlugins.getEJBMetadata(getContainer()).getName());
        super.destroy();
        for (Iterator iterator = proxies.keySet().iterator(); iterator.hasNext();) {
            String proxyName = (String) iterator.next();
            EJBProxyFactory ejbProxyFactory = getEJBProxyFactory(proxyName);
            log.debug("Destroying EJBProxyFactory: proxyName=" + proxyName + " ejbName=" + EJBPlugins.getEJBMetadata(getContainer()).getName());
            ejbProxyFactory.destroy();
            // @todo uncomment this when the ejb proxy factories are rewriten to be full components
            //ejbProxyFactory.setContainer(null);
        }
        proxies.clear();
    }

    public void addEJBProxyFactory(String name, EJBProxyFactory ejbProxyFactory) {
        proxies.put(name, ejbProxyFactory);
    }

    public EJBProxyFactory getEJBProxyFactory(String name) {
        return (EJBProxyFactory) proxies.get(name);
    }

    public void setThreadEJBProxyFactory(String name) {
        if (name == null) {
            // todo this seems wrong
            name = "local";
        }
        EJBProxyFactory ejbProxyFactory = getEJBProxyFactory(name);
        if (ejbProxyFactory == null) {
            throw new EJBException("Unknown proxy factory: name=" + name);
        }
        threadEJBProxyFactory.set(ejbProxyFactory);
    }

    public EJBProxyFactory getThreadEJBProxyFactory() {
        return (EJBProxyFactory) threadEJBProxyFactory.get();
    }
}

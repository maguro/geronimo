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
package org.apache.geronimo.deployment.plan;

import java.net.URL;
import java.util.List;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.loader.ClassSpace;
import org.apache.geronimo.deployment.service.ClassSpaceMetadata;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2003/08/20 22:37:17 $
 */
public class CreateClassSpace implements DeploymentTask {
    private final Log log = LogFactory.getLog(this.getClass());
    private final MBeanServer server;
    private final ClassSpaceMetadata metadata;
    private ObjectName actualName;

    public CreateClassSpace(MBeanServer server, ClassSpaceMetadata metadata) {
        this.server = server;
        this.metadata = metadata;
    }

    public boolean canRun() throws DeploymentException {
        return true;
    }

    public void perform() throws DeploymentException {
        ObjectName name = metadata.getName();
        List urls = metadata.getUrls();
        if (!server.isRegistered(name)) {
            try {
                // @todo add trace logging
                // @todo use metadata to determine implementation
                ClassSpace space = new ClassSpace(metadata.getName().toString(), (URL[]) urls.toArray(new URL[urls.size()]));
                actualName = server.registerMBean(space, name).getObjectName();
            } catch (RuntimeException e) {
                throw new DeploymentException(e);
            } catch (InstanceAlreadyExistsException e) {
                throw new DeploymentException(e);
            } catch (MBeanRegistrationException e) {
                throw new DeploymentException(e);
            } catch (NotCompliantMBeanException e) {
                throw new DeploymentException(e);
            }
        } else {
            try {
                server.invoke(name, "addURLs", new Object[]{urls}, new String[]{"java.util.List"});
            } catch (InstanceNotFoundException e) {
                throw new DeploymentException(e);
            } catch (MBeanException e) {
                throw new DeploymentException(e);
            } catch (ReflectionException e) {
                throw new DeploymentException(e);
            }
        }
    }

    public void undo() {
        if (actualName != null) {
            try {
                server.unregisterMBean(actualName);
            } catch (InstanceNotFoundException e) {
                log.warn("ClassSpace MBean was already removed " + actualName, e);
                return;
            } catch (MBeanRegistrationException e) {
                log.error("Error while unregistering ClassSpace MBean " + actualName, e);
            }
        }
    }

    public String toString() {
        return "CreateClassSpace " + metadata.getName();
    }
}

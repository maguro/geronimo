/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.deployment.tools.loader;

import java.net.URL;
import java.net.URLClassLoader;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class DeployableFactory {
    public static DeployableObject createDeployable(URL moduleURL) throws DDBeanCreateException {
        ClassLoader cl = new URLClassLoader(new URL[] {moduleURL}, ClassLoader.getSystemClassLoader());
        if (cl.getResource("META-INF/application.xml") != null) {
            // EAR file
//            return new ApplicationDeployable(moduleFile.toURL());
            throw new UnsupportedOperationException();
        } else if (cl.getResource("META-INF/application-client.xml") != null) {
            // Application Client
            return new ClientDeployable(moduleURL);
        } else if (cl.getResource("WEB-INF/web.xml") != null) {
            // WAR
            return new WebDeployable(moduleURL);
        } else if (cl.getResource("META-INF/ejb-jar.xml") != null) {
            // EJB Jar
            throw new UnsupportedOperationException();
        } else if (cl.getResource("META-INF/ra.xml") != null) {
            // Connector
            return new ConnectorDeployable(moduleURL);
        } else {
            throw new DDBeanCreateException("Unrecognized archive: " + moduleURL);
        }
    }
}

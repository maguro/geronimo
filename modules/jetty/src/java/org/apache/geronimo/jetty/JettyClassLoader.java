/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.jetty;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @version $Rev$ $Date$
 */
public class JettyClassLoader extends URLClassLoader {
    private final boolean contextPriorityClassLoader;
    private final ClassLoader parent;

    public JettyClassLoader(URL[] urls, ClassLoader parent, boolean contextPriorityClassLoader) {
        super(urls, parent);

        if (parent == null) {
            throw new IllegalArgumentException("Parent class loader is null");
        }

        // hold on to the parent so we don't have to go throught the security check each time
        this.parent = parent;
        this.contextPriorityClassLoader = contextPriorityClassLoader;
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        if (!contextPriorityClassLoader) {
            return super.loadClass(name);
        }

        // first check if this class has already been loaded
        Class clazz = findLoadedClass(name);
        if (clazz != null) {
            return clazz;
        }

        // try to load the class from this class loader
        try {
            clazz = findClass(name);
        } catch (ClassNotFoundException ignored) {
        }
        if (clazz != null) {
            return clazz;
        }

        // that didn't work... try the parent
        return parent.loadClass(name);
    }

    public URL getResource(String name) {
        if (!contextPriorityClassLoader) {
            return super.getResource(name);
        }

        // try to load the resource from this class loader
        URL url = findResource(name);
        if (url != null) {
            return url;
        }

        // that didn't work... try the parent
        return parent.getResource(name);
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        if (name.startsWith("java.") ||
                name.startsWith("javax.") ||
                // todo we can't enable this because geronimo demo and tools are in this package
                // name.startsWith("org.apache.geronimo.") ||
                name.startsWith("org.mortbay.") ||
                name.startsWith("org.xml.") ||
                name.startsWith("org.w3c.")) {
            throw new ClassNotFoundException(name);
        }
        return super.findClass(name);
    }

    public URL findResource(String name) {
        if (name.startsWith("java/") ||
                name.startsWith("javax/") ||
                // todo we can't enable this because geronimo demo and tools are in this package
                // name.startsWith("org/apache/geronimo/") ||
                name.startsWith("org/mortbay/") ||
                name.startsWith("org/xml/") ||
                name.startsWith("org/w3c/")) {
            return null;
        }
        return super.findResource(name);
    }
}

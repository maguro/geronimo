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

package org.apache.geronimo.kernel;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManagerImpl;
import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;

import javax.management.ObjectName;
import java.util.Collections;

/**
 * @version $Rev$ $Date$
 */
public class ConfigTest extends TestCase {
    private ObjectName gbeanName1;
    private Kernel kernel;
    private ObjectName gbeanName2;
    private GBeanData config;

    public void testOnlineConfig() throws Exception {

        // load and start the config
        kernel.loadGBean(config, this.getClass().getClassLoader());
        kernel.startGBean(config.getName());
        kernel.invoke(config.getName(), "loadGBeans", new Object[] {null}, new String[] {ManageableAttributeStore.class.getName()});
        kernel.invoke(config.getName(), "startRecursiveGBeans");

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(config.getName()));
        assertNotNull(kernel.getAttribute(config.getName(), "configurationClassLoader"));

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbeanName1));
        int state = kernel.getGBeanState(gbeanName2);
        assertEquals(State.RUNNING_INDEX, state);
        assertEquals(new Integer(1), kernel.getAttribute(gbeanName1, "finalInt"));
        assertEquals("1234", kernel.getAttribute(gbeanName1, "value"));
        assertEquals(new Integer(3), kernel.getAttribute(gbeanName2, "finalInt"));

        kernel.setAttribute(gbeanName2, "mutableInt", new Integer(44));
        assertEquals(new Integer(44), kernel.getAttribute(gbeanName2, "mutableInt"));

        kernel.invoke(gbeanName2, "doSetMutableInt", new Object[]{new Integer(55)}, new String[]{"int"});
        assertEquals(new Integer(55), kernel.getAttribute(gbeanName2, "mutableInt"));

        assertEquals("no endpoint", kernel.invoke(gbeanName1, "checkEndpoint", null, null));
        assertEquals("endpointCheck", kernel.invoke(gbeanName2, "checkEndpoint", null, null));

        assertEquals(new Integer(0), kernel.invoke(gbeanName1, "checkEndpointCollection", null, null));
        assertEquals(new Integer(1), kernel.invoke(gbeanName2, "checkEndpointCollection", null, null));

        kernel.setAttribute(gbeanName2, "endpointMutableInt", new Integer(99));
        assertEquals(new Integer(99), kernel.getAttribute(gbeanName2, "endpointMutableInt"));
        assertEquals(new Integer(99), kernel.getAttribute(gbeanName1, "mutableInt"));

        kernel.stopGBean(config.getName());
        try {
            kernel.getAttribute(gbeanName1, "value");
            fail();
        } catch (GBeanNotFoundException e) {
            // ok
        }
        assertEquals(State.STOPPED_INDEX, kernel.getGBeanState(config.getName()));
        kernel.unloadGBean(config.getName());
        assertFalse(kernel.isLoaded(config.getName()));
    }

    public void testAddToConfig() throws Exception {

        // load and start the config
        kernel.loadGBean(config, this.getClass().getClassLoader());
        kernel.startGBean(config.getName());
        kernel.invoke(config.getName(), "loadGBeans", new Object[] {null}, new String[] {ManageableAttributeStore.class.getName()});
        kernel.invoke(config.getName(), "startRecursiveGBeans");

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(config.getName()));
        assertNotNull(kernel.getAttribute(config.getName(), "configurationClassLoader"));

        ObjectName gbeanName3 = new ObjectName("geronimo.test:name=MyMockGMBean3");
        try {
            kernel.getGBeanState(gbeanName3);
            fail("Gbean should not be found yet");
        } catch (GBeanNotFoundException e) {
        }
        GBeanData mockBean3 = new GBeanData(gbeanName3, MockGBean.getGBeanInfo());
        mockBean3.setAttribute("value", "1234");
        mockBean3.setAttribute("name", "child");
        mockBean3.setAttribute("finalInt", new Integer(1));
        kernel.invoke(config.getName(), "addGBean", new Object[]{mockBean3,Boolean.TRUE}, new String[]{GBeanData.class.getName(), boolean.class.getName()});

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbeanName3));
        assertEquals(new Integer(1), kernel.getAttribute(gbeanName3, "finalInt"));
        assertEquals("1234", kernel.getAttribute(gbeanName3, "value"));
        assertEquals("child", kernel.getAttribute(gbeanName3, "name"));


    }

    protected void setUp() throws Exception {
        kernel = KernelFactory.newInstance().createKernel("test");
        kernel.boot();

        ObjectName configurationManagerName = new ObjectName(":j2eeType=ConfigurationManager,name=Basic");
        GBeanData configurationManagerData = new GBeanData(configurationManagerName, ConfigurationManagerImpl.GBEAN_INFO);
        kernel.loadGBean(configurationManagerData, getClass().getClassLoader());
        kernel.startGBean(configurationManagerName);

        gbeanName1 = new ObjectName("geronimo.test:name=MyMockGMBean1");
        GBeanData mockBean1 = new GBeanData(gbeanName1, MockGBean.getGBeanInfo());
        mockBean1.setAttribute("value", "1234");
        mockBean1.setAttribute("name", "child");
        mockBean1.setAttribute("finalInt", new Integer(1));

        gbeanName2 = new ObjectName("geronimo.test:name=MyMockGMBean2");
        GBeanData mockBean2 = new GBeanData(gbeanName2, MockGBean.getGBeanInfo());
        mockBean2.setAttribute("value", "5678");
        mockBean2.setAttribute("name", "Parent");
        mockBean2.setAttribute("finalInt", new Integer(3));
        mockBean2.setReferencePatterns("MockEndpoint", Collections.singleton(gbeanName1));
        mockBean2.setReferencePatterns("EndpointCollection", Collections.singleton(gbeanName1));

        byte[] state = Configuration.storeGBeans(new GBeanData[] {mockBean1, mockBean2});

        Artifact id = new Artifact("geronimo", "test", "1", "car", true);
        ObjectName configName = Configuration.getConfigurationObjectName(id);

        // create the config gbean data
        config = new GBeanData(Configuration.getConfigurationObjectName(id), Configuration.GBEAN_INFO);
        Environment environment = new Environment();
        environment.setConfigId(id);
        config.setAttribute("environment", environment);
        config.setAttribute("gBeanState", state);
        config.setName(configName);
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
    }
}

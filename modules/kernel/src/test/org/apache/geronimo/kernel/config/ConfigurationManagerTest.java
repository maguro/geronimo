/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.kernel.config;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.Version;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationManagerTest extends TestCase {
    private Kernel kernel;
    private Artifact artifact1;
    private Artifact artifact2;
    private Artifact artifact3;
    private Map configurations = new HashMap();
    private ConfigurationManager configurationManager;

    public void test() throws Exception {
        Configuration configuration = configurationManager.loadConfiguration(artifact3);
        assertEquals(artifact3, configuration.getId());
        assertTrue(configurationManager.isLoaded(artifact3));
        assertTrue(configurationManager.isLoaded(artifact2));
        assertTrue(configurationManager.isLoaded(artifact1));
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact1))) ;
    }

    protected void setUp() throws Exception {
        super.setUp();

        kernel = KernelFactory.newInstance().createKernel("test");
        kernel.boot();

        GBeanData artifactManagerData = buildGBeanData("name", "ArtifactManager", DefaultArtifactManager.GBEAN_INFO);
        kernel.loadGBean(artifactManagerData, getClass().getClassLoader());
        kernel.startGBean(artifactManagerData.getAbstractName());
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(artifactManagerData.getAbstractName()));
        ArtifactManager artifactManager = (ArtifactManager) kernel.getGBean(artifactManagerData.getAbstractName());

        TestConfigStore configStore = new TestConfigStore();
        TestRepository testRepository = new TestRepository();
        DefaultArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, testRepository);

        artifact1 = new Artifact("test", "1", "1.1", "bar");
        artifact2 = new Artifact("test", "2", "2.2", "bar");
        artifact3 = new Artifact("test", "3", "3.3", "bar");

        Environment e1 = new Environment();
        e1.setConfigId(artifact1);
        ConfigurationData configurationData1 = new ConfigurationData(e1, kernel.getNaming());
        configurationData1.setConfigurationStore(configStore);
        configurations.put(artifact1, configurationData1);

        Environment e2 = new Environment();
        e2.setConfigId(artifact2);
        e2.addDependency(new Artifact("test", "1", (Version) null, "bar"), ImportType.ALL);
        ConfigurationData configurationData2 = new ConfigurationData(e2, kernel.getNaming());
        configurationData2.setConfigurationStore(configStore);
        configurations.put(artifact2, configurationData2);

        Environment e3 = new Environment();
        e3.setConfigId(artifact3);
        e3.addDependency(new Artifact("test", "2", (Version) null, "bar"), ImportType.ALL);
        ConfigurationData configurationData3 = new ConfigurationData(e3, kernel.getNaming());
        configurationData3.setConfigurationStore(configStore);
        configurations.put(artifact3, configurationData3);


        configurationManager = new KernelConfigurationManager(kernel,
                Collections.singleton(configStore),
                null,
                null,
                artifactManager,
                artifactResolver,
                Collections.singleton(testRepository),
                KernelConfigurationManager.class.getClassLoader());
    }

    private class TestConfigStore implements ConfigurationStore {
        public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
            throw new UnsupportedOperationException();
        }

        public void uninstall(Artifact configId) throws NoSuchConfigException, IOException {
            throw new UnsupportedOperationException();
        }

        public ConfigurationData loadConfiguration(Artifact configId) throws IOException, InvalidConfigException, NoSuchConfigException {
            return (ConfigurationData) configurations.get(configId);
        }

        public boolean containsConfiguration(Artifact configId) {
            return configurations.containsKey(configId);
        }

        public String getObjectName() {
            throw new UnsupportedOperationException();
        }

        public List listConfigurations() {
            throw new UnsupportedOperationException();
        }

        public File createNewConfigurationDir(Artifact configId) throws ConfigurationAlreadyExistsException {
            throw new UnsupportedOperationException();
        }

        public URL resolve(Artifact configId, String moduleName, URI uri) throws NoSuchConfigException, MalformedURLException {
            throw new UnsupportedOperationException();
        }
    }

    private GBeanData buildGBeanData(String key, String value, GBeanInfo info) {
        AbstractName abstractName = kernel.getNaming().createRootName(new Artifact("test", "foo", "1", "car"), value, key);
        return new GBeanData(abstractName, info);
    }

    private class TestRepository implements ListableRepository {
        public SortedSet list() {
            return new TreeSet(configurations.keySet());
        }

        public SortedSet list(String groupId, String artifactId, String type) {
            TreeSet artifacts = new TreeSet();
            for (Iterator iterator = configurations.keySet().iterator(); iterator.hasNext();) {
                Artifact artifact = (Artifact) iterator.next();
                if (artifact.getGroupId().equals(groupId) &&
                        artifact.getArtifactId().equals(artifactId) &&
                        artifact.getType().equals(type)) {
                    artifacts.add(artifact);
                }
            }
            return artifacts;
        }

        public boolean contains(Artifact artifact) {
            return configurations.containsKey(artifact);
        }

        public File getLocation(Artifact artifact) {
            throw new UnsupportedOperationException();
        }

        public LinkedHashSet getDependencies(Artifact artifact) {
            return new LinkedHashSet();
        }
    }
}

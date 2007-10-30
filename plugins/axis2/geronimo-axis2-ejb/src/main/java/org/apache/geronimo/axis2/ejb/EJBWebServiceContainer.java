/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.axis2.ejb;

import java.net.URL;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.util.JavaUtils;
import org.apache.geronimo.axis2.Axis2WebServiceContainer;
import org.apache.geronimo.axis2.AxisServiceGenerator;
import org.apache.geronimo.jaxws.JAXWSAnnotationProcessor;
import org.apache.geronimo.jaxws.JNDIResolver;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.openejb.DeploymentInfo;

/**
 * @version $Rev$ $Date$
 */
public class EJBWebServiceContainer extends Axis2WebServiceContainer {

    private DeploymentInfo deploymnetInfo;
    
    public EJBWebServiceContainer(PortInfo portInfo,
                                  String endpointClassName,
                                  ClassLoader classLoader,
                                  Context context,
                                  URL configurationBaseUrl,
                                  DeploymentInfo deploymnetInfo) {
        super(portInfo, endpointClassName, classLoader, context, configurationBaseUrl);
        this.deploymnetInfo = deploymnetInfo;
    }
    
    @Override
    public void init() throws Exception { 
        super.init();
        
        String rootContext = null;
        String servicePath = null;
        String location = trimContext(this.portInfo.getLocation());
        int pos = location.indexOf('/');     
        if (pos > 0) {
            rootContext = location.substring(0, pos);
            servicePath = location.substring(pos + 1);
        } else {
            rootContext = "/";
            servicePath = location;
        }
              
        this.configurationContext.setServicePath(servicePath);
        //need to setContextRoot after servicePath as cachedServicePath is only built 
        //when setContextRoot is called.
        this.configurationContext.setContextRoot(rootContext); 
        
        // configure handlers
        try {
            configureHandlers();
        } catch (Exception e) {
            throw new WebServiceException("Error configuring handlers", e);
        }
    }
    
    @Override
    protected AxisServiceGenerator createServiceGenerator() {
        AxisServiceGenerator serviceGenerator = super.createServiceGenerator();
        EJBMessageReceiver messageReceiver = 
            new EJBMessageReceiver(this, this.endpointClass, this.deploymnetInfo);
        serviceGenerator.setMessageReceiver(messageReceiver);
        return serviceGenerator;
    }
        
    public synchronized void injectHandlers() {
        if (this.annotationProcessor != null) {
            // assume injection was already done
            return;
        }
        
        WebServiceContext wsContext = null;
        try {
            InitialContext ctx = new InitialContext();
            wsContext = (WebServiceContext) ctx.lookup("java:comp/WebServiceContext");
        } catch (NamingException e) {
            throw new WebServiceException("Failed to lookup WebServiceContext", e);
        }
        
        this.annotationProcessor = new JAXWSAnnotationProcessor(new JNDIResolver(), wsContext);
        super.injectHandlers();
    }
    
    @Override
    public void destroy() {
        // call handler preDestroy
        destroyHandlers();
        
        super.destroy();
    }
}

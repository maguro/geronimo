/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.geronimo.test;

import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.HandlerChain;
import javax.jws.soap.SOAPBinding;

@WebService
@Stateless(mappedName="JAXWSBean")
@HandlerChain(file="handlers.xml")
@SOAPBinding(style=SOAPBinding.Style.RPC, 
             use=SOAPBinding.Use.LITERAL,
             parameterStyle=SOAPBinding.ParameterStyle.WRAPPED
)
public class JAXWSBean implements JAXWSGreeter { 

    public String greetMe(String me) {
        System.out.println("i'm a ejb ws: " + me);
        if (!"foo bar".equals(me)) {
            throw new RuntimeException("Wrong parameter");
        }
        return "Hello " + me;
    }
    
}

/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.console.jmsmanager;

import javax.jms.Destination;

/**
 * @version $Rev$ $Date$
 */
public enum DestinationType {
    Queue {

        @Override
        public String[] getConnectionFactoryInterfaces() {
            return new String[] { "javax.jms.QueueConnectionFactory", "javax.jms.ConnectionFactory" };
        }

        @Override
        public String getDestinationInterface() {
            return "javax.jms.Queue";
        }

        @Override
        public Class<? extends Destination> getTypeClass() {
            return javax.jms.Queue.class;
        }
    },
    Topic {

        @Override
        public String[] getConnectionFactoryInterfaces() {
            return new String[] { "javax.jms.TopicConnectionFactory", "javax.jms.ConnectionFactory" };
        }

        @Override
        public String getDestinationInterface() {
            return "javax.jms.Topic";
        }

        @Override
        public Class<? extends Destination> getTypeClass() {
            return javax.jms.Topic.class;
        }
    };

    public abstract String getDestinationInterface();

    public abstract String[] getConnectionFactoryInterfaces();

    public abstract Class<? extends Destination> getTypeClass();
}
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.quartz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * Endpoints are stored in a LRU list with a default capacity of 1000. If the list is full, 
 * then endpoints are removed and should be recreated.
 * 
 *  We simulate this behavior with a capacity of 1 element.
 * 
 * @version 
 */
public class QuartzCronRouteWithSmallCacheTest extends CamelTestSupport {

	private List<Object> list = new ArrayList<Object>();
	
	@Test
    public void testQuartzCronRouteWithSmallCache() throws Exception {
		synchronized (this) {
			for(int i = 0; i< 100; i++) {
				wait(100);
				if(list.size() > 4) {
					break;
				}
				
			}	
		}
		
		assertTrue("Cron was triggered only " + list.size() + " times", list.size() > 4);
    }

	
	@Override
	protected CamelContext createCamelContext() throws Exception {
		CamelContext context =  new DefaultCamelContext(){
			@Override
			public Map<String, String> getProperties() {
				Map<String, String> res =  super.getProperties();
				res.put(Exchange.MAXIMUM_ENDPOINT_CACHE_SIZE, "1");
				return res;
			}
		};
		
		return context;
	}
	
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                // START SNIPPET: e1
                // triggers every 2th second at precise 00,02,04,06..58
                // notice we must use + as space when configured using URI parameter
            	try {
                from("quartz://myGroup/myTimerName?cron=0/2+*+*+*+*+?").process(new Processor() {
                	@Override
                	public void process(Exchange exchange) throws Exception {
                		context().createProducerTemplate().send("direct:a", exchange);
                		context().createProducerTemplate().send("direct:a", exchange);
                		list.add(new Object());
                	}
                });
                from("direct:a").process(new Processor() {
					
					@Override
					public void process(Exchange arg0) throws Exception {
					}
				});
                } catch(Exception e) {
            		e.printStackTrace();
            	}
                // END SNIPPET: e1
            }
        };
    }
}
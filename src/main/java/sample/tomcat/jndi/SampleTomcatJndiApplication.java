/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.tomcat.jndi;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.startup.Tomcat;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.util.ClassUtils;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class SampleTomcatJndiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SampleTomcatJndiApplication.class, args);
	}

	@Bean
	public TomcatEmbeddedServletContainerFactory tomcatFactory() {
		return new TomcatEmbeddedServletContainerFactory() {

            @Override
			protected TomcatEmbeddedServletContainer getTomcatEmbeddedServletContainer(
					Tomcat tomcat) {
				tomcat.enableNaming();
				TomcatEmbeddedServletContainer container = super.getTomcatEmbeddedServletContainer(tomcat);
				Container context = container.getTomcat().getHost().findChild("");
				ClassUtils.overrideThreadContextClassLoader(context.getLoader().getClassLoader());
				return container;
			}

			@Override
			protected void postProcessContext(Context context) {
				ContextResource resource = new ContextResource();
				resource.setName("jdbc/myDataSource");
				resource.setType(DataSource.class.getName());
				resource.setProperty("driverClassName", "org.hsqldb.jdbc.JDBCDriver");
				resource.setProperty("url", "jdbc:hsqldb:mem:mymemdb");

				context.getNamingResources().addResource(resource);
			}
		};
	}

	@Bean(destroyMethod="")
	public DataSource jndiDataSource() throws IllegalArgumentException, NamingException {
		JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
		bean.setJndiName("java:comp/env/jdbc/myDataSource");
		bean.setProxyInterface(DataSource.class);
		bean.setLookupOnStartup(false);
		bean.afterPropertiesSet();
		return (DataSource)bean.getObject();
	}
}

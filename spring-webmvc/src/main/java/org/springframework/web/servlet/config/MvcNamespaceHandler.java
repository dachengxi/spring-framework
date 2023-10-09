/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.config;

import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * {@link NamespaceHandler} for Spring MVC configuration namespace.
 *
 * @author Keith Donald
 * @author Jeremy Grelle
 * @author Sebastien Deleuze
 * @since 3.0
 */
public class MvcNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		// 注册<mvc:annotation-driven/>标签解析器，注册MVC相关的处理器
		registerBeanDefinitionParser("annotation-driven", new AnnotationDrivenBeanDefinitionParser());
		// 注册<mvc:default-servlet-handler/>标签解析器，注册DefaultServletHttpRequestHandler，用来处理所有http请求，包括静态资源，会将静态资源转发给web服务器默认的Servlet处理，非静态资源由DispatcherServlet处理
		registerBeanDefinitionParser("default-servlet-handler", new DefaultServletHandlerBeanDefinitionParser());
		// 注册<mvc:interceptors/>标签解析器，用来解析配置的拦截器
		registerBeanDefinitionParser("interceptors", new InterceptorsBeanDefinitionParser());
		// 注册<mvc:resources/>标签解析器，可以由自己来处理静态资源
		registerBeanDefinitionParser("resources", new ResourcesBeanDefinitionParser());
		// 注册<mvc:view-controller/>标签解析器
		registerBeanDefinitionParser("view-controller", new ViewControllerBeanDefinitionParser());
		// 注册<mvc:redirect-view-controller/>标签解析器
		registerBeanDefinitionParser("redirect-view-controller", new ViewControllerBeanDefinitionParser());
		// 注册<mvc:status-controller/>标签解析器
		registerBeanDefinitionParser("status-controller", new ViewControllerBeanDefinitionParser());
		// 注册<mvc:view-resolvers/>标签解析器
		registerBeanDefinitionParser("view-resolvers", new ViewResolversBeanDefinitionParser());
		registerBeanDefinitionParser("tiles-configurer", new TilesConfigurerBeanDefinitionParser());
		registerBeanDefinitionParser("freemarker-configurer", new FreeMarkerConfigurerBeanDefinitionParser());
		registerBeanDefinitionParser("groovy-configurer", new GroovyMarkupConfigurerBeanDefinitionParser());
		registerBeanDefinitionParser("script-template-configurer", new ScriptTemplateConfigurerBeanDefinitionParser());
		registerBeanDefinitionParser("cors", new CorsBeanDefinitionParser());
	}

}

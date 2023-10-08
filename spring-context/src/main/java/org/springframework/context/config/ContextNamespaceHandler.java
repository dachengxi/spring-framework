/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.context.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.context.annotation.AnnotationConfigBeanDefinitionParser;
import org.springframework.context.annotation.ComponentScanBeanDefinitionParser;

/**
 * {@link org.springframework.beans.factory.xml.NamespaceHandler}
 * for the '{@code context}' namespace.
 *
 * context命名空间的处理器
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 */
public class ContextNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		// 注册Bean定义解析器：<context:property-placeholder>标签对应的解析器，用来处理properties配置文件
		registerBeanDefinitionParser("property-placeholder", new PropertyPlaceholderBeanDefinitionParser());
		// 注册Bean定义解析器：<context:property-override>标签对应的解析器，允许使用properties文件的形式对Bean的属性进行替换
		registerBeanDefinitionParser("property-override", new PropertyOverrideBeanDefinitionParser());
		// 注册Bean定义解析器：<context:annotation-config>标签对应的解析器，注册ConfigurationClassPostProcessor、AutowiredAnnotationBeanPostProcessor、
		// CommonAnnotationBeanPostProcessor、PersistenceAnnotationBeanPostProcessor、EventListenerMethodProcessor、DefaultEventListenerFactory等几个处理器到容器中
		registerBeanDefinitionParser("annotation-config", new AnnotationConfigBeanDefinitionParser());
		// 注册Bean定义解析器：<context:component-scan>标签对应的解析器，主要扫描@Component注解的Bean，其他的@Repository、@Service、@Controller
		// 等注解也是@Component类型的注解，也会被扫描到，然后将扫描到的Bean定义注册到容器中。同时也会注册ConfigurationClassPostProcessor、AutowiredAnnotationBeanPostProcessor、
		// CommonAnnotationBeanPostProcessor、PersistenceAnnotationBeanPostProcessor、EventListenerMethodProcessor、DefaultEventListenerFactory等几个处理器到容器中，
		// 有了<context-annotation-config/>的功能
		registerBeanDefinitionParser("component-scan", new ComponentScanBeanDefinitionParser());
		// 注册Bean定义解析器：<context:load-time-weaver>标签对应的解析器
		registerBeanDefinitionParser("load-time-weaver", new LoadTimeWeaverBeanDefinitionParser());
		// 注册Bean定义解析器：<context:spring-configured>标签对应的解析器
		registerBeanDefinitionParser("spring-configured", new SpringConfiguredBeanDefinitionParser());
		// 注册Bean定义解析器：<context:mbean-export>标签对应的解析器
		registerBeanDefinitionParser("mbean-export", new MBeanExportBeanDefinitionParser());
		// 注册Bean定义解析器：<context:mbean-server>标签对应的解析器
		registerBeanDefinitionParser("mbean-server", new MBeanServerBeanDefinitionParser());
	}

}

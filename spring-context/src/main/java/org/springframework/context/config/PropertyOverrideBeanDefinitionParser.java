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

package org.springframework.context.config;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;

/**
 * Parser for the &lt;context:property-override/&gt; element.
 *
 * 解析<context:property-override/>元素
 * @author Juergen Hoeller
 * @author Dave Syer
 * @since 2.5.2
 */
class PropertyOverrideBeanDefinitionParser extends AbstractPropertyLoadingBeanDefinitionParser {

	@Override
	protected Class<?> getBeanClass(Element element) {
		return PropertyOverrideConfigurer.class;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		// // 调用父类的公共方法解析公共的一些属性
		super.doParse(element, parserContext, builder);

		// ignore-unresolvable属性，是否忽略为解析到的属性
		builder.addPropertyValue("ignoreInvalidKeys",
				Boolean.valueOf(element.getAttribute("ignore-unresolvable")));

	}

}

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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;

/**
 * Abstract parser for &lt;context:property-.../&gt; elements.
 *
 * 用来解析<context:properety-placeholder/>和<context:property-override/>标签
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @author Dave Syer
 * @since 2.5.2
 */
abstract class AbstractPropertyLoadingBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	@Override
	protected boolean shouldGenerateId() {
		return true;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		// 配置文件的位置
		String location = element.getAttribute("location");
		if (StringUtils.hasLength(location)) {
			// 解析配置文件位置中的占位符
			location = parserContext.getReaderContext().getEnvironment().resolvePlaceholders(location);
			// 可能会有多个位置
			String[] locations = StringUtils.commaDelimitedListToStringArray(location);
			// 将配置文件位置放到Bean定义的属性值集合中缓存起来
			builder.addPropertyValue("locations", locations);
		}

		// properties-ref属性，本地properties配置
		String propertiesRef = element.getAttribute("properties-ref");
		if (StringUtils.hasLength(propertiesRef)) {
			builder.addPropertyReference("properties", propertiesRef);
		}

		// file-encoding属性，文件编码
		String fileEncoding = element.getAttribute("file-encoding");
		if (StringUtils.hasLength(fileEncoding)) {
			builder.addPropertyValue("fileEncoding", fileEncoding);
		}

		// order属性，顺序
		String order = element.getAttribute("order");
		if (StringUtils.hasLength(order)) {
			builder.addPropertyValue("order", Integer.valueOf(order));
		}

		// ignore-resource-not-found属性，是否忽略找不到的属性文件
		builder.addPropertyValue("ignoreResourceNotFound",
				Boolean.valueOf(element.getAttribute("ignore-resource-not-found")));

		// local-override属性，是否本地覆盖，如果为true，则properties-ref的属性将覆盖location加载的属性
		builder.addPropertyValue("localOverride",
				Boolean.valueOf(element.getAttribute("local-override")));

		// Bean定义的角色是框架内部使用
		builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
	}

}

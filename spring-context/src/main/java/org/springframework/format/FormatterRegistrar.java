/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.format;

import org.springframework.core.convert.converter.Converter;

/**
 * Registers {@link Converter Converters} and {@link Formatter Formatters} with
 * a FormattingConversionService through the {@link FormatterRegistry} SPI.
 *
 * 格式化器注册器
 *
 * @author Keith Donald
 * @since 3.1
 */
public interface FormatterRegistrar {

	/**
	 * Register Formatters and Converters with a FormattingConversionService
	 * through a FormatterRegistry SPI.
	 * 向格式化器注册中心注册格式化器
	 * @param registry the FormatterRegistry instance to use.
	 */
	void registerFormatters(FormatterRegistry registry);

}

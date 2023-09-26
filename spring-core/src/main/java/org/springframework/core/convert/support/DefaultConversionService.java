/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.core.convert.support;

import java.nio.charset.Charset;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.lang.Nullable;

/**
 * A specialization of {@link GenericConversionService} configured by default
 * with converters appropriate for most environments.
 *
 * <p>Designed for direct instantiation but also exposes the static
 * {@link #addDefaultConverters(ConverterRegistry)} utility method for ad-hoc
 * use against any {@code ConverterRegistry} instance.
 *
 * 默认的转换服务实现
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 3.1
 */
public class DefaultConversionService extends GenericConversionService {

	@Nullable
	private static volatile DefaultConversionService sharedInstance;


	/**
	 * Create a new {@code DefaultConversionService} with the set of
	 * {@linkplain DefaultConversionService#addDefaultConverters(ConverterRegistry) default converters}.
	 */
	public DefaultConversionService() {
		addDefaultConverters(this);
	}


	/**
	 * Return a shared default {@code ConversionService} instance,
	 * lazily building it once needed.
	 * <p><b>NOTE:</b> We highly recommend constructing individual
	 * {@code ConversionService} instances for customization purposes.
	 * This accessor is only meant as a fallback for code paths which
	 * need simple type coercion but cannot access a longer-lived
	 * {@code ConversionService} instance any other way.
	 * @return the shared {@code ConversionService} instance (never {@code null})
	 * @since 4.3.5
	 */
	public static ConversionService getSharedInstance() {
		DefaultConversionService cs = sharedInstance;
		if (cs == null) {
			synchronized (DefaultConversionService.class) {
				cs = sharedInstance;
				if (cs == null) {
					cs = new DefaultConversionService();
					sharedInstance = cs;
				}
			}
		}
		return cs;
	}

	/**
	 * Add converters appropriate for most environments.
	 * 添加一些默认的转换器
	 * @param converterRegistry the registry of converters to add to
	 * (must also be castable to ConversionService, e.g. being a {@link ConfigurableConversionService})
	 * @throws ClassCastException if the given ConverterRegistry could not be cast to a ConversionService
	 */
	public static void addDefaultConverters(ConverterRegistry converterRegistry) {
		// 添加一些标量的转换器
		addScalarConverters(converterRegistry);
		// 添加一些集合相关的转换器
		addCollectionConverters(converterRegistry);

		// ByteBuffer转换器
		converterRegistry.addConverter(new ByteBufferConverter((ConversionService) converterRegistry));
		// 字符串到时区的转换器
		converterRegistry.addConverter(new StringToTimeZoneConverter());
		// ZoneId到时区的转换器
		converterRegistry.addConverter(new ZoneIdToTimeZoneConverter());
		// ZonedDateTime到Calendar的转换器
		converterRegistry.addConverter(new ZonedDateTimeToCalendarConverter());

		// 对象到对象的转换器
		converterRegistry.addConverter(new ObjectToObjectConverter());
		// 实体的ID到实体的引用的转换器
		converterRegistry.addConverter(new IdToEntityConverter((ConversionService) converterRegistry));
		// 对象到字符串的转换器
		converterRegistry.addConverter(new FallbackObjectToStringConverter());
		// 对象到Optional的转换器
		converterRegistry.addConverter(new ObjectToOptionalConverter((ConversionService) converterRegistry));
	}

	/**
	 * Add common collection converters.
	 * 添加一些集合相关的转换器
	 * @param converterRegistry the registry of converters to add to
	 * (must also be castable to ConversionService, e.g. being a {@link ConfigurableConversionService})
	 * @throws ClassCastException if the given ConverterRegistry could not be cast to a ConversionService
	 * @since 4.2.3
	 */
	public static void addCollectionConverters(ConverterRegistry converterRegistry) {
		ConversionService conversionService = (ConversionService) converterRegistry;

		// 数组到集合的转换器
		converterRegistry.addConverter(new ArrayToCollectionConverter(conversionService));
		// 集合到数组的转换器
		converterRegistry.addConverter(new CollectionToArrayConverter(conversionService));

		// 数组到数组的转换器
		converterRegistry.addConverter(new ArrayToArrayConverter(conversionService));
		// 集合到集合的转换器
		converterRegistry.addConverter(new CollectionToCollectionConverter(conversionService));
		// Map到Map的转换器
		converterRegistry.addConverter(new MapToMapConverter(conversionService));

		// 数组到字符串的转换器
		converterRegistry.addConverter(new ArrayToStringConverter(conversionService));
		// 字符串到数组的转换器
		converterRegistry.addConverter(new StringToArrayConverter(conversionService));

		// 数组到对象的转换器
		converterRegistry.addConverter(new ArrayToObjectConverter(conversionService));
		// 对象到数组的转换器
		converterRegistry.addConverter(new ObjectToArrayConverter(conversionService));

		// 集合到字符串的转换器
		converterRegistry.addConverter(new CollectionToStringConverter(conversionService));
		// 字符串到集合的转换器
		converterRegistry.addConverter(new StringToCollectionConverter(conversionService));

		// 集合到对象的转换器
		converterRegistry.addConverter(new CollectionToObjectConverter(conversionService));
		// 对象到集合的转换器
		converterRegistry.addConverter(new ObjectToCollectionConverter(conversionService));

		// 流转换器
		converterRegistry.addConverter(new StreamConverter(conversionService));
	}

	/**
	 * 添加一些标量的转换器
	 * @param converterRegistry
	 */
	private static void addScalarConverters(ConverterRegistry converterRegistry) {
		// Number到Number的转换器
		converterRegistry.addConverterFactory(new NumberToNumberConverterFactory());

		// 字符串到Number的转换器
		converterRegistry.addConverterFactory(new StringToNumberConverterFactory());
		// Number到String的转换器
		converterRegistry.addConverter(Number.class, String.class, new ObjectToStringConverter());

		// 字符串到字符的转换器
		converterRegistry.addConverter(new StringToCharacterConverter());
		// 字符到字符串的转换器
		converterRegistry.addConverter(Character.class, String.class, new ObjectToStringConverter());

		// Number到字符的转换器
		converterRegistry.addConverter(new NumberToCharacterConverter());
		// 字符到Number的转换器
		converterRegistry.addConverterFactory(new CharacterToNumberFactory());

		// 字符串到布尔类型的转换器
		converterRegistry.addConverter(new StringToBooleanConverter());
		// 布尔类型到字符串的转换器
		converterRegistry.addConverter(Boolean.class, String.class, new ObjectToStringConverter());

		// 字符串到枚举的转换器
		converterRegistry.addConverterFactory(new StringToEnumConverterFactory());
		// 枚举到字符串的转换器
		converterRegistry.addConverter(new EnumToStringConverter((ConversionService) converterRegistry));

		// 整形到枚举的转换器
		converterRegistry.addConverterFactory(new IntegerToEnumConverterFactory());
		// 枚举到整形的转换器
		converterRegistry.addConverter(new EnumToIntegerConverter((ConversionService) converterRegistry));

		// 字符串到Locale的转换器
		converterRegistry.addConverter(new StringToLocaleConverter());
		// Local到字符串的转换器
		converterRegistry.addConverter(Locale.class, String.class, new ObjectToStringConverter());

		// 字符串到Charset的转换器
		converterRegistry.addConverter(new StringToCharsetConverter());
		// Charset到字符串的转换器
		converterRegistry.addConverter(Charset.class, String.class, new ObjectToStringConverter());

		// 字符串到货币的转换器
		converterRegistry.addConverter(new StringToCurrencyConverter());
		// 货币到字符串的转换器
		converterRegistry.addConverter(Currency.class, String.class, new ObjectToStringConverter());

		// 字符串到Properties的转换器
		converterRegistry.addConverter(new StringToPropertiesConverter());
		// Properties到字符串的转换器
		converterRegistry.addConverter(new PropertiesToStringConverter());

		// 字符串到UUID的转换器
		converterRegistry.addConverter(new StringToUUIDConverter());
		// UUID到字符串的转换器
		converterRegistry.addConverter(UUID.class, String.class, new ObjectToStringConverter());
	}

}

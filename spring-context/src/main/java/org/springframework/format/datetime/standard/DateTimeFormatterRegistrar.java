/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.format.datetime.standard;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.annotation.DateTimeFormat.ISO;

/**
 * Configures the JSR-310 <code>java.time</code> formatting system for use with Spring.
 *
 * 日期时间相关的格式化器注册器
 *
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @since 4.0
 * @see #setDateStyle
 * @see #setTimeStyle
 * @see #setDateTimeStyle
 * @see #setUseIsoFormat
 * @see org.springframework.format.FormatterRegistrar#registerFormatters
 * @see org.springframework.format.datetime.DateFormatterRegistrar
 * @see org.springframework.format.datetime.joda.DateTimeFormatterFactoryBean
 */
public class DateTimeFormatterRegistrar implements FormatterRegistrar {

	private enum Type {DATE, TIME, DATE_TIME}


	/**
	 * User-defined formatters.
	 * 格式化器
	 */
	private final Map<Type, DateTimeFormatter> formatters = new EnumMap<>(Type.class);

	/**
	 * Factories used when specific formatters have not been specified.
	 * 日期时间格式化器工厂
	 */
	private final Map<Type, DateTimeFormatterFactory> factories = new EnumMap<>(Type.class);


	public DateTimeFormatterRegistrar() {
		for (Type type : Type.values()) {
			this.factories.put(type, new DateTimeFormatterFactory());
		}
	}


	/**
	 * Set whether standard ISO formatting should be applied to all date/time types.
	 * Default is "false" (no).
	 * <p>If set to "true", the "dateStyle", "timeStyle" and "dateTimeStyle"
	 * properties are effectively ignored.
	 */
	public void setUseIsoFormat(boolean useIsoFormat) {
		this.factories.get(Type.DATE).setIso(useIsoFormat ? ISO.DATE : ISO.NONE);
		this.factories.get(Type.TIME).setIso(useIsoFormat ? ISO.TIME : ISO.NONE);
		this.factories.get(Type.DATE_TIME).setIso(useIsoFormat ? ISO.DATE_TIME : ISO.NONE);
	}

	/**
	 * Set the default format style of {@link java.time.LocalDate} objects.
	 * Default is {@link java.time.format.FormatStyle#SHORT}.
	 */
	public void setDateStyle(FormatStyle dateStyle) {
		this.factories.get(Type.DATE).setDateStyle(dateStyle);
	}

	/**
	 * Set the default format style of {@link java.time.LocalTime} objects.
	 * Default is {@link java.time.format.FormatStyle#SHORT}.
	 */
	public void setTimeStyle(FormatStyle timeStyle) {
		this.factories.get(Type.TIME).setTimeStyle(timeStyle);
	}

	/**
	 * Set the default format style of {@link java.time.LocalDateTime} objects.
	 * Default is {@link java.time.format.FormatStyle#SHORT}.
	 */
	public void setDateTimeStyle(FormatStyle dateTimeStyle) {
		this.factories.get(Type.DATE_TIME).setDateTimeStyle(dateTimeStyle);
	}

	/**
	 * Set the formatter that will be used for objects representing date values.
	 * <p>This formatter will be used for the {@link LocalDate} type.
	 * When specified, the {@link #setDateStyle dateStyle} and
	 * {@link #setUseIsoFormat useIsoFormat} properties will be ignored.
	 * @param formatter the formatter to use
	 * @see #setTimeFormatter
	 * @see #setDateTimeFormatter
	 */
	public void setDateFormatter(DateTimeFormatter formatter) {
		this.formatters.put(Type.DATE, formatter);
	}

	/**
	 * Set the formatter that will be used for objects representing time values.
	 * <p>This formatter will be used for the {@link LocalTime} and {@link OffsetTime}
	 * types. When specified, the {@link #setTimeStyle timeStyle} and
	 * {@link #setUseIsoFormat useIsoFormat} properties will be ignored.
	 * @param formatter the formatter to use
	 * @see #setDateFormatter
	 * @see #setDateTimeFormatter
	 */
	public void setTimeFormatter(DateTimeFormatter formatter) {
		this.formatters.put(Type.TIME, formatter);
	}

	/**
	 * Set the formatter that will be used for objects representing date and time values.
	 * <p>This formatter will be used for {@link LocalDateTime}, {@link ZonedDateTime}
	 * and {@link OffsetDateTime} types. When specified, the
	 * {@link #setDateTimeStyle dateTimeStyle} and
	 * {@link #setUseIsoFormat useIsoFormat} properties will be ignored.
	 * @param formatter the formatter to use
	 * @see #setDateFormatter
	 * @see #setTimeFormatter
	 */
	public void setDateTimeFormatter(DateTimeFormatter formatter) {
		this.formatters.put(Type.DATE_TIME, formatter);
	}


	@Override
	public void registerFormatters(FormatterRegistry registry) {
		// 注册日期时间相关的转换器
		DateTimeConverters.registerConverters(registry);

		DateTimeFormatter df = getFormatter(Type.DATE);
		DateTimeFormatter tf = getFormatter(Type.TIME);
		DateTimeFormatter dtf = getFormatter(Type.DATE_TIME);

		// Efficient ISO_LOCAL_* variants for printing since they are twice as fast...

		// 注册LocalDate类型字段的打印器和解析器
		registry.addFormatterForFieldType(LocalDate.class,
				new TemporalAccessorPrinter(
						df == DateTimeFormatter.ISO_DATE ? DateTimeFormatter.ISO_LOCAL_DATE : df),
				new TemporalAccessorParser(LocalDate.class, df));

		// 注册LocalTime类型字段的打印器和解析器
		registry.addFormatterForFieldType(LocalTime.class,
				new TemporalAccessorPrinter(
						tf == DateTimeFormatter.ISO_TIME ? DateTimeFormatter.ISO_LOCAL_TIME : tf),
				new TemporalAccessorParser(LocalTime.class, tf));

		// 注册LocalDateTime类型字段的打印器和解析器
		registry.addFormatterForFieldType(LocalDateTime.class,
				new TemporalAccessorPrinter(
						dtf == DateTimeFormatter.ISO_DATE_TIME ? DateTimeFormatter.ISO_LOCAL_DATE_TIME : dtf),
				new TemporalAccessorParser(LocalDateTime.class, dtf));

		// 注册ZonedDateTime类型字段的打印器和解析器
		registry.addFormatterForFieldType(ZonedDateTime.class,
				new TemporalAccessorPrinter(dtf),
				new TemporalAccessorParser(ZonedDateTime.class, dtf));

		// 注册OffsetDateTime类型字段的打印器和解析器
		registry.addFormatterForFieldType(OffsetDateTime.class,
				new TemporalAccessorPrinter(dtf),
				new TemporalAccessorParser(OffsetDateTime.class, dtf));

		// 注册OffsetTime类型字段的打印器和解析器
		registry.addFormatterForFieldType(OffsetTime.class,
				new TemporalAccessorPrinter(tf),
				new TemporalAccessorParser(OffsetTime.class, tf));

		// 注册Instant类型字段的格式化器
		registry.addFormatterForFieldType(Instant.class, new InstantFormatter());
		// 注册Period类型字段的格式化器
		registry.addFormatterForFieldType(Period.class, new PeriodFormatter());
		// 注册Duration类型字段的格式化器
		registry.addFormatterForFieldType(Duration.class, new DurationFormatter());
		// 注册Year类型字段的格式化器
		registry.addFormatterForFieldType(Year.class, new YearFormatter());
		// 注册Month类型字段的格式化器
		registry.addFormatterForFieldType(Month.class, new MonthFormatter());
		// 注册YearMonth类型字段的格式化器
		registry.addFormatterForFieldType(YearMonth.class, new YearMonthFormatter());
		// 注册MonthDay类型字段的格式化器
		registry.addFormatterForFieldType(MonthDay.class, new MonthDayFormatter());

		// 注册DateTimeFormat注解的格式化器工厂
		registry.addFormatterForFieldAnnotation(new Jsr310DateTimeFormatAnnotationFormatterFactory());
	}

	private DateTimeFormatter getFormatter(Type type) {
		DateTimeFormatter formatter = this.formatters.get(type);
		if (formatter != null) {
			return formatter;
		}
		DateTimeFormatter fallbackFormatter = getFallbackFormatter(type);
		return this.factories.get(type).createDateTimeFormatter(fallbackFormatter);
	}

	private DateTimeFormatter getFallbackFormatter(Type type) {
		switch (type) {
			case DATE: return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
			case TIME: return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
			default: return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
		}
	}

}

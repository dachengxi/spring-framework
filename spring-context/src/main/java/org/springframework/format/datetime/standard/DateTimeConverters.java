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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.format.datetime.DateFormatterRegistrar;

/**
 * Installs lower-level type converters required to integrate
 * JSR-310 support into Spring's field formatting system.
 *
 * <p>Note: {@link DateTimeFormatterRegistrar} installs these converters but
 * does not rely on them for its formatters. They are just being registered
 * for custom conversion scenarios between different JSR-310 value types
 * and also between {@link java.util.Calendar} and JSR-310 value types.
 *
 * 日期时间转换器
 *
 * @author Juergen Hoeller
 * @since 4.0.1
 */
final class DateTimeConverters {

	private DateTimeConverters() {
	}


	/**
	 * Install the converters into the converter registry.
	 * 注册日期时间转换器到转换器注册中心
	 * @param registry the converter registry
	 */
	public static void registerConverters(ConverterRegistry registry) {
		// 使用日期转换器注册器添加转换器到转换器注册中心，Date相关的
		DateFormatterRegistrar.addDateConverters(registry);

		// 注册LocalDateTime转LocalDate转换器
		registry.addConverter(new LocalDateTimeToLocalDateConverter());
		// 注册LocalDateTime转LocalTime转换器
		registry.addConverter(new LocalDateTimeToLocalTimeConverter());
		// 注册ZonedDateTime转LocalDate转换器
		registry.addConverter(new ZonedDateTimeToLocalDateConverter());
		// 注册ZonedDateTime转LocalTime转换器
		registry.addConverter(new ZonedDateTimeToLocalTimeConverter());
		// 注册ZonedDateTime转LocalDateTime转换器
		registry.addConverter(new ZonedDateTimeToLocalDateTimeConverter());
		// 注册ZonedDateTime转OffsetDateTime转换器
		registry.addConverter(new ZonedDateTimeToOffsetDateTimeConverter());
		// 注册ZonedDateTime转Instant转换器
		registry.addConverter(new ZonedDateTimeToInstantConverter());
		// 注册OffsetDateTime转LocalDate转换器
		registry.addConverter(new OffsetDateTimeToLocalDateConverter());
		// 注册OffsetDateTime转LocalTime转换器
		registry.addConverter(new OffsetDateTimeToLocalTimeConverter());
		// 注册OffsetDateTime转LocalDateTime转换器
		registry.addConverter(new OffsetDateTimeToLocalDateTimeConverter());
		// 注册OffsetDateTime转ZonedDateTime转换器
		registry.addConverter(new OffsetDateTimeToZonedDateTimeConverter());
		// 注册OffsetDateTime转Instant转换器
		registry.addConverter(new OffsetDateTimeToInstantConverter());
		// 注册Calendar转ZonedDateTime转换器
		registry.addConverter(new CalendarToZonedDateTimeConverter());
		// 注册Calendar转OffsetDateTime转换器
		registry.addConverter(new CalendarToOffsetDateTimeConverter());
		// 注册Calendar转LocalDate转换器
		registry.addConverter(new CalendarToLocalDateConverter());
		// 注册Calendar转LocalTime转换器
		registry.addConverter(new CalendarToLocalTimeConverter());
		// 注册Calendar转LocalDateTime转换器
		registry.addConverter(new CalendarToLocalDateTimeConverter());
		// 注册Calendar转Instant转换器
		registry.addConverter(new CalendarToInstantConverter());
		// 注册Long转Instant转换器
		registry.addConverter(new LongToInstantConverter());
		// 注册Instant转Long转换器
		registry.addConverter(new InstantToLongConverter());
	}

	private static ZonedDateTime calendarToZonedDateTime(Calendar source) {
		if (source instanceof GregorianCalendar) {
			return ((GregorianCalendar) source).toZonedDateTime();
		}
		else {
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(source.getTimeInMillis()),
					source.getTimeZone().toZoneId());
		}
	}


	/**
	 * LocalDateTime转LocalDate转换器
	 */
	private static class LocalDateTimeToLocalDateConverter implements Converter<LocalDateTime, LocalDate> {

		@Override
		public LocalDate convert(LocalDateTime source) {
			return source.toLocalDate();
		}
	}


	/**
	 * LocalDateTime转LocalTime转换器
	 */
	private static class LocalDateTimeToLocalTimeConverter implements Converter<LocalDateTime, LocalTime> {

		@Override
		public LocalTime convert(LocalDateTime source) {
			return source.toLocalTime();
		}
	}


	/**
	 * ZonedDateTime转LocalDate转换器
	 */
	private static class ZonedDateTimeToLocalDateConverter implements Converter<ZonedDateTime, LocalDate> {

		@Override
		public LocalDate convert(ZonedDateTime source) {
			return source.toLocalDate();
		}
	}


	/**
	 * ZonedDateTime转LocalTime转换器
	 */
	private static class ZonedDateTimeToLocalTimeConverter implements Converter<ZonedDateTime, LocalTime> {

		@Override
		public LocalTime convert(ZonedDateTime source) {
			return source.toLocalTime();
		}
	}


	/**
	 * ZonedDateTime转LocalDateTime转换器
	 */
	private static class ZonedDateTimeToLocalDateTimeConverter implements Converter<ZonedDateTime, LocalDateTime> {

		@Override
		public LocalDateTime convert(ZonedDateTime source) {
			return source.toLocalDateTime();
		}
	}

	/**
	 * ZonedDateTime转OffsetDateTime转换器
	 */
	private static class ZonedDateTimeToOffsetDateTimeConverter implements Converter<ZonedDateTime, OffsetDateTime> {

		@Override
		public OffsetDateTime convert(ZonedDateTime source) {
			return source.toOffsetDateTime();
		}
	}


	/**
	 * ZonedDateTime转Instant转换器
	 */
	private static class ZonedDateTimeToInstantConverter implements Converter<ZonedDateTime, Instant> {

		@Override
		public Instant convert(ZonedDateTime source) {
			return source.toInstant();
		}
	}


	/**
	 * OffsetDateTime转LocalDate转换器
	 */
	private static class OffsetDateTimeToLocalDateConverter implements Converter<OffsetDateTime, LocalDate> {

		@Override
		public LocalDate convert(OffsetDateTime source) {
			return source.toLocalDate();
		}
	}


	/**
	 * OffsetDateTime转LocalTime转换器
	 */
	private static class OffsetDateTimeToLocalTimeConverter implements Converter<OffsetDateTime, LocalTime> {

		@Override
		public LocalTime convert(OffsetDateTime source) {
			return source.toLocalTime();
		}
	}


	/**
	 * OffsetDateTime转LocalDateTime转换器
	 */
	private static class OffsetDateTimeToLocalDateTimeConverter implements Converter<OffsetDateTime, LocalDateTime> {

		@Override
		public LocalDateTime convert(OffsetDateTime source) {
			return source.toLocalDateTime();
		}
	}


	/**
	 * OffsetDateTime转ZonedDateTime转换器
	 */
	private static class OffsetDateTimeToZonedDateTimeConverter implements Converter<OffsetDateTime, ZonedDateTime> {

		@Override
		public ZonedDateTime convert(OffsetDateTime source) {
			return source.toZonedDateTime();
		}
	}


	/**
	 * OffsetDateTime转Instant转换器
	 */
	private static class OffsetDateTimeToInstantConverter implements Converter<OffsetDateTime, Instant> {

		@Override
		public Instant convert(OffsetDateTime source) {
			return source.toInstant();
		}
	}


	/**
	 * Calendar转ZonedDateTime转换器
	 */
	private static class CalendarToZonedDateTimeConverter implements Converter<Calendar, ZonedDateTime> {

		@Override
		public ZonedDateTime convert(Calendar source) {
			return calendarToZonedDateTime(source);
		}
	}


	/**
	 * Calendar转OffsetDateTime转换器
	 */
	private static class CalendarToOffsetDateTimeConverter implements Converter<Calendar, OffsetDateTime> {

		@Override
		public OffsetDateTime convert(Calendar source) {
			return calendarToZonedDateTime(source).toOffsetDateTime();
		}
	}


	/**
	 * Calendar转LocalDate转换器
	 */
	private static class CalendarToLocalDateConverter implements Converter<Calendar, LocalDate> {

		@Override
		public LocalDate convert(Calendar source) {
			return calendarToZonedDateTime(source).toLocalDate();
		}
	}


	/**
	 * Calendar转LocalTime转换器
	 */
	private static class CalendarToLocalTimeConverter implements Converter<Calendar, LocalTime> {

		@Override
		public LocalTime convert(Calendar source) {
			return calendarToZonedDateTime(source).toLocalTime();
		}
	}

	/**
	 * Calendar转LocalDateTime转换器
	 */

	private static class CalendarToLocalDateTimeConverter implements Converter<Calendar, LocalDateTime> {

		@Override
		public LocalDateTime convert(Calendar source) {
			return calendarToZonedDateTime(source).toLocalDateTime();
		}
	}


	/**
	 * Calendar转Instant转换器
	 */
	private static class CalendarToInstantConverter implements Converter<Calendar, Instant> {

		@Override
		public Instant convert(Calendar source) {
			return calendarToZonedDateTime(source).toInstant();
		}
	}


	/**
	 * Long转Instant转换器
	 */
	private static class LongToInstantConverter implements Converter<Long, Instant> {

		@Override
		public Instant convert(Long source) {
			return Instant.ofEpochMilli(source);
		}
	}


	/**
	 * Instant转Long转换器
	 */
	private static class InstantToLongConverter implements Converter<Instant, Long> {

		@Override
		public Long convert(Instant source) {
			return source.toEpochMilli();
		}
	}

}

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

package org.springframework.http.converter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

/**
 * Strategy interface for converting from and to HTTP requests and responses.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 3.0
 * @param <T> the converted object type
 *
 * Http消息转换器，将Http请求转换为需要的对象类型或者将返回的对象类型转换为Http响应，比如我们需要使用HttpMessageConverter在调用
 * 具体的Controller方法之前将请求体中的数据转换成具体的Java对象；在Controller方法处理完之后将返回的Java对象转换为响应体中的数据
 */
public interface HttpMessageConverter<T> {

	/**
	 * Indicates whether the given class can be read by this converter.
	 * @param clazz the class to test for readability
	 * @param mediaType the media type to read (can be {@code null} if not specified);
	 * typically the value of a {@code Content-Type} header.
	 * @return {@code true} if readable; {@code false} otherwise
	 *
	 * 是否可以被当前转换器读取
	 */
	boolean canRead(Class<?> clazz, @Nullable MediaType mediaType);

	/**
	 * Indicates whether the given class can be written by this converter.
	 * @param clazz the class to test for writability
	 * @param mediaType the media type to write (can be {@code null} if not specified);
	 * typically the value of an {@code Accept} header.
	 * @return {@code true} if writable; {@code false} otherwise
	 *
	 * 是否可以被当前转换器写
	 */
	boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType);

	/**
	 * Return the list of media types supported by this converter. The list may
	 * not apply to every possible target element type and calls to this method
	 * should typically be guarded via {@link #canWrite(Class, MediaType)
	 * canWrite(clazz, null}. The list may also exclude MIME types supported
	 * only for a specific class. Alternatively, use
	 * {@link #getSupportedMediaTypes(Class)} for a more precise list.
	 * @return the list of supported media types
	 *
	 * 当前转换器支持的MediaType
	 */
	List<MediaType> getSupportedMediaTypes();

	/**
	 * Return the list of media types supported by this converter for the given
	 * class. The list may differ from {@link #getSupportedMediaTypes()} if the
	 * converter does not support the given Class or if it supports it only for
	 * a subset of media types.
	 * @param clazz the type of class to check
	 * @return the list of media types supported for the given class
	 * @since 5.3.4
	 *
	 * 当前转换器支持的MediaType
	 */
	default List<MediaType> getSupportedMediaTypes(Class<?> clazz) {
		return (canRead(clazz, null) || canWrite(clazz, null) ?
				getSupportedMediaTypes() : Collections.emptyList());
	}

	/**
	 * Read an object of the given type from the given input message, and returns it.
	 * @param clazz the type of object to return. This type must have previously been passed to the
	 * {@link #canRead canRead} method of this interface, which must have returned {@code true}.
	 * @param inputMessage the HTTP input message to read from
	 * @return the converted object
	 * @throws IOException in case of I/O errors
	 * @throws HttpMessageNotReadableException in case of conversion errors
	 *
	 * 从Http请求中读取数据，并转换成给定的Java类型对象
	 */
	T read(Class<? extends T> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException;

	/**
	 * Write an given object to the given output message.
	 * @param t the object to write to the output message. The type of this object must have previously been
	 * passed to the {@link #canWrite canWrite} method of this interface, which must have returned {@code true}.
	 * @param contentType the content type to use when writing. May be {@code null} to indicate that the
	 * default content type of the converter must be used. If not {@code null}, this media type must have
	 * previously been passed to the {@link #canWrite canWrite} method of this interface, which must have
	 * returned {@code true}.
	 * @param outputMessage the message to write to
	 * @throws IOException in case of I/O errors
	 * @throws HttpMessageNotWritableException in case of conversion errors
	 *
	 * 将给定的Java类型对象按照给定的MediaType写到Http响应中
	 */
	void write(T t, @Nullable MediaType contentType, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException;

}

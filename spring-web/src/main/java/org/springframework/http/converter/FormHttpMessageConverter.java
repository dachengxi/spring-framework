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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.mail.internet.MimeUtility;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

/**
 * Implementation of {@link HttpMessageConverter} to read and write 'normal' HTML
 * forms and also to write (but not read) multipart data (e.g. file uploads).
 *
 * <p>In other words, this converter can read and write the
 * {@code "application/x-www-form-urlencoded"} media type as
 * {@link MultiValueMap MultiValueMap&lt;String, String&gt;}, and it can also
 * write (but not read) the {@code "multipart/form-data"} and
 * {@code "multipart/mixed"} media types as
 * {@link MultiValueMap MultiValueMap&lt;String, Object&gt;}.
 *
 * <h3>Multipart Data</h3>
 *
 * <p>By default, {@code "multipart/form-data"} is used as the content type when
 * {@linkplain #write writing} multipart data. As of Spring Framework 5.2 it is
 * also possible to write multipart data using other multipart subtypes such as
 * {@code "multipart/mixed"} and {@code "multipart/related"}, as long as the
 * multipart subtype is registered as a {@linkplain #getSupportedMediaTypes
 * supported media type} <em>and</em> the desired multipart subtype is specified
 * as the content type when {@linkplain #write writing} the multipart data. Note
 * that {@code "multipart/mixed"} is registered as a supported media type by
 * default.
 *
 * <p>When writing multipart data, this converter uses other
 * {@link HttpMessageConverter HttpMessageConverters} to write the respective
 * MIME parts. By default, basic converters are registered for byte array,
 * {@code String}, and {@code Resource}. These can be overridden via
 * {@link #setPartConverters} or augmented via {@link #addPartConverter}.
 *
 * <h3>Examples</h3>
 *
 * <p>The following snippet shows how to submit an HTML form using the
 * {@code "multipart/form-data"} content type.
 *
 * <pre class="code">
 * RestTemplate restTemplate = new RestTemplate();
 * // AllEncompassingFormHttpMessageConverter is configured by default
 *
 * MultiValueMap&lt;String, Object&gt; form = new LinkedMultiValueMap&lt;&gt;();
 * form.add("field 1", "value 1");
 * form.add("field 2", "value 2");
 * form.add("field 2", "value 3");
 * form.add("field 3", 4);  // non-String form values supported as of 5.1.4
 *
 * restTemplate.postForLocation("https://example.com/myForm", form);</pre>
 *
 * <p>The following snippet shows how to do a file upload using the
 * {@code "multipart/form-data"} content type.
 *
 * <pre class="code">
 * MultiValueMap&lt;String, Object&gt; parts = new LinkedMultiValueMap&lt;&gt;();
 * parts.add("field 1", "value 1");
 * parts.add("file", new ClassPathResource("myFile.jpg"));
 *
 * restTemplate.postForLocation("https://example.com/myFileUpload", parts);</pre>
 *
 * <p>The following snippet shows how to do a file upload using the
 * {@code "multipart/mixed"} content type.
 *
 * <pre class="code">
 * MultiValueMap&lt;String, Object&gt; parts = new LinkedMultiValueMap&lt;&gt;();
 * parts.add("field 1", "value 1");
 * parts.add("file", new ClassPathResource("myFile.jpg"));
 *
 * HttpHeaders requestHeaders = new HttpHeaders();
 * requestHeaders.setContentType(MediaType.MULTIPART_MIXED);
 *
 * restTemplate.postForLocation("https://example.com/myFileUpload",
 *     new HttpEntity&lt;&gt;(parts, requestHeaders));</pre>
 *
 * <p>The following snippet shows how to do a file upload using the
 * {@code "multipart/related"} content type.
 *
 * <pre class="code">
 * MediaType multipartRelated = new MediaType("multipart", "related");
 *
 * restTemplate.getMessageConverters().stream()
 *     .filter(FormHttpMessageConverter.class::isInstance)
 *     .map(FormHttpMessageConverter.class::cast)
 *     .findFirst()
 *     .orElseThrow(() -&gt; new IllegalStateException("Failed to find FormHttpMessageConverter"))
 *     .addSupportedMediaTypes(multipartRelated);
 *
 * MultiValueMap&lt;String, Object&gt; parts = new LinkedMultiValueMap&lt;&gt;();
 * parts.add("field 1", "value 1");
 * parts.add("file", new ClassPathResource("myFile.jpg"));
 *
 * HttpHeaders requestHeaders = new HttpHeaders();
 * requestHeaders.setContentType(multipartRelated);
 *
 * restTemplate.postForLocation("https://example.com/myFileUpload",
 *     new HttpEntity&lt;&gt;(parts, requestHeaders));</pre>
 *
 * <h3>Miscellaneous</h3>
 *
 * <p>Some methods in this class were inspired by
 * {@code org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity}.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.0
 * @see org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
 * @see org.springframework.util.MultiValueMap
 *
 * 用来读写一般的HTML表单数据，对于myltipart数据只可以写，不能读。支持的类型：application/x-www-form-urlencoded和multipart/form-data
 */
public class FormHttpMessageConverter implements HttpMessageConverter<MultiValueMap<String, ?>> {

	/**
	 * The default charset used by the converter.
	 */
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private static final MediaType DEFAULT_FORM_DATA_MEDIA_TYPE =
			new MediaType(MediaType.APPLICATION_FORM_URLENCODED, DEFAULT_CHARSET);


	private List<MediaType> supportedMediaTypes = new ArrayList<>();

	private List<HttpMessageConverter<?>> partConverters = new ArrayList<>();

	private Charset charset = DEFAULT_CHARSET;

	@Nullable
	private Charset multipartCharset;


	public FormHttpMessageConverter() {
		this.supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
		this.supportedMediaTypes.add(MediaType.MULTIPART_FORM_DATA);
		this.supportedMediaTypes.add(MediaType.MULTIPART_MIXED);

		this.partConverters.add(new ByteArrayHttpMessageConverter());
		this.partConverters.add(new StringHttpMessageConverter());
		this.partConverters.add(new ResourceHttpMessageConverter());

		applyDefaultCharset();
	}


	/**
	 * Set the list of {@link MediaType} objects supported by this converter.
	 * @see #addSupportedMediaTypes(MediaType...)
	 * @see #getSupportedMediaTypes()
	 */
	public void setSupportedMediaTypes(List<MediaType> supportedMediaTypes) {
		Assert.notNull(supportedMediaTypes, "'supportedMediaTypes' must not be null");
		// Ensure internal list is mutable.
		this.supportedMediaTypes = new ArrayList<>(supportedMediaTypes);
	}

	/**
	 * Add {@link MediaType} objects to be supported by this converter.
	 * <p>The supplied {@code MediaType} objects will be appended to the list
	 * of {@linkplain #getSupportedMediaTypes() supported MediaType objects}.
	 * @param supportedMediaTypes a var-args list of {@code MediaType} objects to add
	 * @since 5.2
	 * @see #setSupportedMediaTypes(List)
	 */
	public void addSupportedMediaTypes(MediaType... supportedMediaTypes) {
		Assert.notNull(supportedMediaTypes, "'supportedMediaTypes' must not be null");
		Assert.noNullElements(supportedMediaTypes, "'supportedMediaTypes' must not contain null elements");
		Collections.addAll(this.supportedMediaTypes, supportedMediaTypes);
	}

	/**
	 * {@inheritDoc}
	 * @see #setSupportedMediaTypes(List)
	 * @see #addSupportedMediaTypes(MediaType...)
	 */
	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return Collections.unmodifiableList(this.supportedMediaTypes);
	}

	/**
	 * Set the message body converters to use. These converters are used to
	 * convert objects to MIME parts.
	 */
	public void setPartConverters(List<HttpMessageConverter<?>> partConverters) {
		Assert.notEmpty(partConverters, "'partConverters' must not be empty");
		this.partConverters = partConverters;
	}

	/**
	 * Return the {@linkplain #setPartConverters configured converters} for MIME
	 * parts.
	 * @since 5.3
	 */
	public List<HttpMessageConverter<?>> getPartConverters() {
		return Collections.unmodifiableList(this.partConverters);
	}

	/**
	 * Add a message body converter. Such a converter is used to convert objects
	 * to MIME parts.
	 */
	public void addPartConverter(HttpMessageConverter<?> partConverter) {
		Assert.notNull(partConverter, "'partConverter' must not be null");
		this.partConverters.add(partConverter);
	}

	/**
	 * Set the default character set to use for reading and writing form data when
	 * the request or response {@code Content-Type} header does not explicitly
	 * specify it.
	 * <p>As of 4.3, this is also used as the default charset for the conversion
	 * of text bodies in a multipart request.
	 * <p>As of 5.0, this is also used for part headers including
	 * {@code Content-Disposition} (and its filename parameter) unless (the mutually
	 * exclusive) {@link #setMultipartCharset multipartCharset} is also set, in
	 * which case part headers are encoded as ASCII and <i>filename</i> is encoded
	 * with the {@code encoded-word} syntax from RFC 2047.
	 * <p>By default this is set to "UTF-8".
	 */
	public void setCharset(@Nullable Charset charset) {
		if (charset != this.charset) {
			this.charset = (charset != null ? charset : DEFAULT_CHARSET);
			applyDefaultCharset();
		}
	}

	/**
	 * Apply the configured charset as a default to registered part converters.
	 */
	private void applyDefaultCharset() {
		for (HttpMessageConverter<?> candidate : this.partConverters) {
			if (candidate instanceof AbstractHttpMessageConverter<?> converter) {
				// Only override default charset if the converter operates with a charset to begin with...
				if (converter.getDefaultCharset() != null) {
					converter.setDefaultCharset(this.charset);
				}
			}
		}
	}

	/**
	 * Set the character set to use when writing multipart data to encode file
	 * names. Encoding is based on the {@code encoded-word} syntax defined in
	 * RFC 2047 and relies on {@code MimeUtility} from {@code jakarta.mail}.
	 * <p>As of 5.0 by default part headers, including {@code Content-Disposition}
	 * (and its filename parameter) will be encoded based on the setting of
	 * {@link #setCharset(Charset)} or {@code UTF-8} by default.
	 * @since 4.1.1
	 * @see <a href="https://en.wikipedia.org/wiki/MIME#Encoded-Word">Encoded-Word</a>
	 */
	public void setMultipartCharset(Charset charset) {
		this.multipartCharset = charset;
	}


	@Override
	public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
		if (!MultiValueMap.class.isAssignableFrom(clazz)) {
			return false;
		}
		if (mediaType == null) {
			return true;
		}
		for (MediaType supportedMediaType : getSupportedMediaTypes()) {
			if (supportedMediaType.getType().equalsIgnoreCase("multipart")) {
				// We can't read multipart, so skip this supported media type.
				continue;
			}
			if (supportedMediaType.includes(mediaType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
		if (!MultiValueMap.class.isAssignableFrom(clazz)) {
			return false;
		}
		if (mediaType == null || MediaType.ALL.equals(mediaType)) {
			return true;
		}
		for (MediaType supportedMediaType : getSupportedMediaTypes()) {
			if (supportedMediaType.isCompatibleWith(mediaType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public MultiValueMap<String, String> read(@Nullable Class<? extends MultiValueMap<String, ?>> clazz,
			HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {

		MediaType contentType = inputMessage.getHeaders().getContentType();
		Charset charset = (contentType != null && contentType.getCharset() != null ?
				contentType.getCharset() : this.charset);
		String body = StreamUtils.copyToString(inputMessage.getBody(), charset);

		String[] pairs = StringUtils.tokenizeToStringArray(body, "&");
		MultiValueMap<String, String> result = new LinkedMultiValueMap<>(pairs.length);
		for (String pair : pairs) {
			int idx = pair.indexOf('=');
			if (idx == -1) {
				result.add(URLDecoder.decode(pair, charset), null);
			}
			else {
				String name = URLDecoder.decode(pair.substring(0, idx), charset);
				String value = URLDecoder.decode(pair.substring(idx + 1), charset);
				result.add(name, value);
			}
		}
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void write(MultiValueMap<String, ?> map, @Nullable MediaType contentType, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {

		if (isMultipart(map, contentType)) {
			writeMultipart((MultiValueMap<String, Object>) map, contentType, outputMessage);
		}
		else {
			writeForm((MultiValueMap<String, Object>) map, contentType, outputMessage);
		}
	}


	private boolean isMultipart(MultiValueMap<String, ?> map, @Nullable MediaType contentType) {
		if (contentType != null) {
			return contentType.getType().equalsIgnoreCase("multipart");
		}
		for (List<?> values : map.values()) {
			for (Object value : values) {
				if (value != null && !(value instanceof String)) {
					return true;
				}
			}
		}
		return false;
	}

	private void writeForm(MultiValueMap<String, Object> formData, @Nullable MediaType contentType,
			HttpOutputMessage outputMessage) throws IOException {

		contentType = getFormContentType(contentType);
		outputMessage.getHeaders().setContentType(contentType);

		Charset charset = contentType.getCharset();
		Assert.notNull(charset, "No charset"); // should never occur

		byte[] bytes = serializeForm(formData, charset).getBytes(charset);
		outputMessage.getHeaders().setContentLength(bytes.length);

		if (outputMessage instanceof StreamingHttpOutputMessage streamingOutputMessage) {
			streamingOutputMessage.setBody(outputStream -> StreamUtils.copy(bytes, outputStream));
		}
		else {
			StreamUtils.copy(bytes, outputMessage.getBody());
		}
	}

	/**
	 * Return the content type used to write forms, given the preferred content type.
	 * By default, this method returns the given content type, but adds the
	 * {@linkplain #setCharset(Charset) charset} if it does not have one.
	 * If {@code contentType} is {@code null},
	 * {@code application/x-www-form-urlencoded; charset=UTF-8} is returned.
	 * <p>Subclasses can override this method to change this behavior.
	 * @param contentType the preferred content type (can be {@code null})
	 * @return the content type to be used
	 * @since 5.2.2
	 */
	protected MediaType getFormContentType(@Nullable MediaType contentType) {
		if (contentType == null) {
			return DEFAULT_FORM_DATA_MEDIA_TYPE;
		}
		else if (contentType.getCharset() == null) {
			return new MediaType(contentType, this.charset);
		}
		else {
			return contentType;
		}
	}

	protected String serializeForm(MultiValueMap<String, Object> formData, Charset charset) {
		StringBuilder builder = new StringBuilder();
		formData.forEach((name, values) -> {
				if (name == null) {
					Assert.isTrue(CollectionUtils.isEmpty(values), "Null name in form data: " + formData);
					return;
				}
				values.forEach(value -> {
					if (builder.length() != 0) {
						builder.append('&');
					}
					builder.append(URLEncoder.encode(name, charset));
					if (value != null) {
						builder.append('=');
						builder.append(URLEncoder.encode(String.valueOf(value), charset));
					}
				});
		});

		return builder.toString();
	}

	private void writeMultipart(
			MultiValueMap<String, Object> parts, @Nullable MediaType contentType, HttpOutputMessage outputMessage)
			throws IOException {

		// If the supplied content type is null, fall back to multipart/form-data.
		// Otherwise rely on the fact that isMultipart() already verified the
		// supplied content type is multipart.
		if (contentType == null) {
			contentType = MediaType.MULTIPART_FORM_DATA;
		}

		Map<String, String> parameters = new LinkedHashMap<>(contentType.getParameters().size() + 2);
		parameters.putAll(contentType.getParameters());

		byte[] boundary = generateMultipartBoundary();
		if (!isFilenameCharsetSet()) {
			if (!this.charset.equals(StandardCharsets.UTF_8) &&
					!this.charset.equals(StandardCharsets.US_ASCII)) {
				parameters.put("charset", this.charset.name());
			}
		}
		parameters.put("boundary", new String(boundary, StandardCharsets.US_ASCII));

		// Add parameters to output content type
		contentType = new MediaType(contentType, parameters);
		outputMessage.getHeaders().setContentType(contentType);

		if (outputMessage instanceof StreamingHttpOutputMessage streamingOutputMessage) {
			streamingOutputMessage.setBody(outputStream -> {
				writeParts(outputStream, parts, boundary);
				writeEnd(outputStream, boundary);
			});
		}
		else {
			writeParts(outputMessage.getBody(), parts, boundary);
			writeEnd(outputMessage.getBody(), boundary);
		}
	}

	/**
	 * When {@link #setMultipartCharset(Charset)} is configured (i.e. RFC 2047,
	 * {@code encoded-word} syntax) we need to use ASCII for part headers, or
	 * otherwise we encode directly using the configured {@link #setCharset(Charset)}.
	 */
	private boolean isFilenameCharsetSet() {
		return (this.multipartCharset != null);
	}

	private void writeParts(OutputStream os, MultiValueMap<String, Object> parts, byte[] boundary) throws IOException {
		for (Map.Entry<String, List<Object>> entry : parts.entrySet()) {
			String name = entry.getKey();
			for (Object part : entry.getValue()) {
				if (part != null) {
					writeBoundary(os, boundary);
					writePart(name, getHttpEntity(part), os);
					writeNewLine(os);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void writePart(String name, HttpEntity<?> partEntity, OutputStream os) throws IOException {
		Object partBody = partEntity.getBody();
		if (partBody == null) {
			throw new IllegalStateException("Empty body for part '" + name + "': " + partEntity);
		}
		Class<?> partType = partBody.getClass();
		HttpHeaders partHeaders = partEntity.getHeaders();
		MediaType partContentType = partHeaders.getContentType();
		for (HttpMessageConverter<?> messageConverter : this.partConverters) {
			if (messageConverter.canWrite(partType, partContentType)) {
				Charset charset = isFilenameCharsetSet() ? StandardCharsets.US_ASCII : this.charset;
				HttpOutputMessage multipartMessage = new MultipartHttpOutputMessage(os, charset);
				multipartMessage.getHeaders().setContentDispositionFormData(name, getFilename(partBody));
				if (!partHeaders.isEmpty()) {
					multipartMessage.getHeaders().putAll(partHeaders);
				}
				((HttpMessageConverter<Object>) messageConverter).write(partBody, partContentType, multipartMessage);
				return;
			}
		}
		throw new HttpMessageNotWritableException("Could not write request: no suitable HttpMessageConverter " +
				"found for request type [" + partType.getName() + "]");
	}

	/**
	 * Generate a multipart boundary.
	 * <p>This implementation delegates to
	 * {@link MimeTypeUtils#generateMultipartBoundary()}.
	 */
	protected byte[] generateMultipartBoundary() {
		return MimeTypeUtils.generateMultipartBoundary();
	}

	/**
	 * Return an {@link HttpEntity} for the given part Object.
	 * @param part the part to return an {@link HttpEntity} for
	 * @return the part Object itself it is an {@link HttpEntity},
	 * or a newly built {@link HttpEntity} wrapper for that part
	 */
	protected HttpEntity<?> getHttpEntity(Object part) {
		return (part instanceof HttpEntity<?> httpEntity ? httpEntity : new HttpEntity<>(part));
	}

	/**
	 * Return the filename of the given multipart part. This value will be used for the
	 * {@code Content-Disposition} header.
	 * <p>The default implementation returns {@link Resource#getFilename()} if the part is a
	 * {@code Resource}, and {@code null} in other cases. Can be overridden in subclasses.
	 * @param part the part to determine the file name for
	 * @return the filename, or {@code null} if not known
	 */
	@Nullable
	protected String getFilename(Object part) {
		if (part instanceof Resource resource) {
			String filename = resource.getFilename();
			if (filename != null && this.multipartCharset != null) {
				filename = MimeDelegate.encode(filename, this.multipartCharset.name());
			}
			return filename;
		}
		else {
			return null;
		}
	}


	private void writeBoundary(OutputStream os, byte[] boundary) throws IOException {
		os.write('-');
		os.write('-');
		os.write(boundary);
		writeNewLine(os);
	}

	private static void writeEnd(OutputStream os, byte[] boundary) throws IOException {
		os.write('-');
		os.write('-');
		os.write(boundary);
		os.write('-');
		os.write('-');
		writeNewLine(os);
	}

	private static void writeNewLine(OutputStream os) throws IOException {
		os.write('\r');
		os.write('\n');
	}


	/**
	 * Implementation of {@link org.springframework.http.HttpOutputMessage} used
	 * to write a MIME multipart.
	 */
	private static class MultipartHttpOutputMessage implements HttpOutputMessage {

		private final OutputStream outputStream;

		private final Charset charset;

		private final HttpHeaders headers = new HttpHeaders();

		private boolean headersWritten = false;

		public MultipartHttpOutputMessage(OutputStream outputStream, Charset charset) {
			this.outputStream = outputStream;
			this.charset = charset;
		}

		@Override
		public HttpHeaders getHeaders() {
			return (this.headersWritten ? HttpHeaders.readOnlyHttpHeaders(this.headers) : this.headers);
		}

		@Override
		public OutputStream getBody() throws IOException {
			writeHeaders();
			return this.outputStream;
		}

		private void writeHeaders() throws IOException {
			if (!this.headersWritten) {
				for (Map.Entry<String, List<String>> entry : this.headers.entrySet()) {
					byte[] headerName = getBytes(entry.getKey());
					for (String headerValueString : entry.getValue()) {
						byte[] headerValue = getBytes(headerValueString);
						this.outputStream.write(headerName);
						this.outputStream.write(':');
						this.outputStream.write(' ');
						this.outputStream.write(headerValue);
						writeNewLine(this.outputStream);
					}
				}
				writeNewLine(this.outputStream);
				this.headersWritten = true;
			}
		}

		private byte[] getBytes(String name) {
			return name.getBytes(this.charset);
		}
	}


	/**
	 * Inner class to avoid a hard dependency on the JavaMail API.
	 */
	private static class MimeDelegate {

		public static String encode(String value, String charset) {
			try {
				return MimeUtility.encodeText(value, charset, null);
			}
			catch (UnsupportedEncodingException ex) {
				throw new IllegalStateException(ex);
			}
		}
	}

}

/*
 * Copyright (c) 2010-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.ning.http.client.providers.apache;

import com.ning.http.client.Cookie;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.providers.ResponseBase;
import com.ning.http.util.AsyncHttpProviderUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ApacheResponse extends ResponseBase {
    private final static String HEADERS_NOT_COMPUTED = "Response's headers hasn't been computed by your AsyncHandler.";

    private final List<Cookie> cookies = new ArrayList<Cookie>();

    public ApacheResponse(HttpResponseStatus status,
                          HttpResponseHeaders headers,
                          List<HttpResponseBodyPart> bodyParts) {
        super(status, headers, bodyParts);
    }

    /* @Override */

    public String getResponseBodyExcerpt(int maxLength) throws IOException {
        return getResponseBodyExcerpt(maxLength, DEFAULT_CHARSET);
    }

    /* @Override */

    public String getResponseBodyExcerpt(int maxLength, String charset) throws IOException {
        String contentType = getContentType();
        if (contentType != null && charset == null) {
            charset = AsyncHttpProviderUtils.parseCharset(contentType);
        }

        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }

        String response = AsyncHttpProviderUtils.contentToString(bodyParts, charset);
        return response.length() <= maxLength ? response : response.substring(0, maxLength);
    }

    /* @Override */
    public List<Cookie> getCookies() {
        if (headers == null) {
            throw new IllegalStateException(HEADERS_NOT_COMPUTED);
        }
        if (cookies.isEmpty()) {
            for (Map.Entry<String, List<String>> header : headers.getHeaders().entrySet()) {
                if (header.getKey().equalsIgnoreCase("Set-Cookie")) {
                    // TODO: ask for parsed header
                    List<String> v = header.getValue();
                    for (String value : v) {
                        Cookie cookie = AsyncHttpProviderUtils.parseCookie(value);
                        cookies.add(cookie);
                    }
                }
            }
        }
        return Collections.unmodifiableList(cookies);
    }

    /**
     * {@inheritDoc}
     */
    /* @Override */
    public boolean hasResponseStatus() {
        return (bodyParts != null ? true : false);
    }

    /**
     * {@inheritDoc}
     */
    /* @Override */
    public boolean hasResponseHeaders() {
        return (headers != null ? true : false);
    }

    /**
     * {@inheritDoc}
     */
    /* @Override */
    public boolean hasResponseBody() {
        return (bodyParts != null && bodyParts.size() > 0 ? true : false);
    }
}

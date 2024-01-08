package com.example.support;

import java.io.PrintStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class Request extends AbstractClientHttpRequest implements AutoCloseable {
    static PrintStream out = System.out;

    @Nullable
    private HttpURLConnection connection;

    private HttpHeaders headers = getHeaders();

    public Request(HttpURLConnection connection) {
        this.connection = connection;
    }

    @Override
    public URI getURI() {
        return null;
    }

    @Override
    public HttpMethod getMethod() {
        return null;
    }

    public void setMethod(String method) {
        try {
            assertNotExecuted();
            this.connection.setRequestMethod(method);
        }
        catch(Exception e) {
            // ignore
        }
    }

    @Override
    public OutputStream getBodyInternal(HttpHeaders headers) {
        return null;
    }

    @Override
    public ClientHttpResponse executeInternal(HttpHeaders headers) {
        return null;
    }

    public Response executeInternal() {
        try {   
            //addHeaders();
            this.connection.connect();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return new Response(this.connection);
    }

    void addHeaders() {
		String method = connection.getRequestMethod();
		if (method.equals("PUT") || method.equals("DELETE")) {
			if (!StringUtils.hasText(this.headers.getFirst(HttpHeaders.ACCEPT))) {
				// Avoid "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2"
				// from HttpUrlConnection which prevents JSON error response details.
				this.headers.set(HttpHeaders.ACCEPT, "*/*");
			}
		}
		this.headers.forEach((headerName, headerValues) -> {
			if (HttpHeaders.COOKIE.equalsIgnoreCase(headerName)) {  // RFC 6265
				String headerValue = StringUtils.collectionToDelimitedString(headerValues, "; ");
				connection.setRequestProperty(headerName, headerValue);
			}
			else {
				for (String headerValue : headerValues) {
					String actualHeaderValue = headerValue != null ? headerValue : "";
					connection.addRequestProperty(headerName, actualHeaderValue);
				}
			}
		});
	}

    public void addHeader(String key, String value) {
        this.headers.add(key, value);
    }

    @Override
    public void close() {
        out.printf("Closing the request");
    }

}
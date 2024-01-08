package com.example.support;

import java.io.IOException;
import java.io.PrintStream;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;


public class Response implements ClientHttpResponse {
    static PrintStream out = System.out;

    @Nullable
    private HttpHeaders headers;

    @Nullable
    private HttpURLConnection connection;

    @Nullable
    private InputStream responseStream;

    public Response(HttpURLConnection connection) {
        this.connection = connection;
    }

    @Override
    public HttpHeaders getHeaders() {
        if (this.headers == null) {
			this.headers = new HttpHeaders();
			// Header field 0 is the status line for most HttpURLConnections, but not on GAE
			String name = this.connection.getHeaderFieldKey(0);
			if (StringUtils.hasLength(name)) {
				this.headers.add(name, this.connection.getHeaderField(0));
			}
			int i = 1;
			while (true) {
				name = this.connection.getHeaderFieldKey(i);
				if (!StringUtils.hasLength(name)) {
					break;
				}
				this.headers.add(name, this.connection.getHeaderField(i));
				i++;
			}
		}
		return this.headers;
    }

    @Override
    public InputStream getBody() throws IOException {
        if (this.responseStream != null) {
            return this.responseStream;
        }
        this.responseStream = this.connection.getErrorStream() == null ? this.connection.getInputStream() : this.connection.getErrorStream();
        return this.responseStream;
    }

    @Override
	public HttpStatusCode getStatusCode() throws IOException {
		return HttpStatusCode.valueOf(this.connection.getResponseCode());
	}

	@Override
	public String getStatusText() throws IOException {
		String result = this.connection.getResponseMessage();
		return (result != null) ? result : "";
    }
	

    @Override 
    public void close() {
        try {
            if (this.responseStream == null) {
                return;
            }
            this.responseStream.close();
            return;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
package com.wizzardo.http.response;

import com.wizzardo.epoll.ByteBufferWrapper;
import com.wizzardo.epoll.readable.ReadableBuilder;
import com.wizzardo.epoll.readable.ReadableByteBuffer;
import com.wizzardo.epoll.readable.ReadableData;
import com.wizzardo.http.HttpConnection;
import com.wizzardo.http.RawHandler;
import com.wizzardo.http.request.Header;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author: wizzardo
 * Date: 3/31/14
 */
public class Response {
    private static final byte[] LINE_SEPARATOR = "\r\n".getBytes();
    private static final byte[] HEADER_SEPARATOR = ": ".getBytes();

    private static ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd-MMM-yyyy kk:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            return format;
        }
    };

    private byte[][] headers = new byte[20][];
    private int headersCount = 0;
    private boolean processed = false;
    private Status status = Status._200;
    private byte[] body;

    public Response setBody(String s) {
        return setBody(s.getBytes());
    }

    public Response setBody(byte[] body) {
        this.body = body;
        setHeader(Header.KEY_CONTENT_LENGTH, String.valueOf(body.length));
        return this;
    }

    public byte[] getBody() {
        return body;
    }

    public Response setStatus(Status status) {
        this.status = status;
        return this;
    }

    public Response setHeader(String key, String value) {
        return setHeader(key.getBytes(), value.getBytes());
    }

    public Response setHeader(Header key, String value) {
        return setHeader(key.bytes, value.getBytes());
    }

    public Response setHeader(Header key, long value) {
        return setHeader(key, String.valueOf(value));
    }

    public Response setHeader(Header key, Header value) {
        return setHeader(key.bytes, value.bytes);
    }

    public Response setHeader(byte[] key, byte[] value) {
        for (int i = 0; i < headersCount; i += 2) {
            if (Arrays.equals(key, headers[i])) {
                headers[i + 1] = value;
                return this;
            }
        }
        appendHeader(key, value);
        return this;
    }

    public Response appendHeader(String key, String value) {
        return appendHeader(key.getBytes(), value.getBytes());
    }

    public Response appendHeader(Header key, String value) {
        return appendHeader(key.bytes, value.getBytes());
    }

    public Response appendHeader(Header key, Header value) {
        return appendHeader(key.bytes, value.bytes);
    }

    public Response appendHeader(byte[] key, byte[] value) {
        if (headersCount + 1 >= headers.length)
            increaseHeadersSize();

        headers[headersCount++] = key;
        headers[headersCount++] = value;

        return this;
    }

    private void increaseHeadersSize() {
        byte[][] temp = new byte[headers.length * 3 / 2][];
        System.arraycopy(headers, 0, temp, 0, headers.length);
        headers = temp;
    }

    public ReadableData toReadableBytes() {
        return buildResponse();
    }

    protected ReadableBuilder buildResponse() {
        ReadableBuilder builder = new ReadableBuilder(status.header);
        for (int i = 0; i < headersCount; i += 2) {
            builder.append(headers[i])
                    .append(HEADER_SEPARATOR)
                    .append(headers[i + 1])
                    .append(LINE_SEPARATOR);
        }

        builder.append(LINE_SEPARATOR);
        if (body != null)
            builder.append(body);
        return builder;
    }

    public boolean isProcessed() {
        return processed;
    }

    public OutputStream getOutputStream(HttpConnection connection) {
        if (!processed) {
            connection.getOutputStream();
            connection.write(toReadableBytes());
            processed = true;
        }

        return connection.getOutputStream();
    }

    public void setCookie(String key, String value, String path) {
        Date expdate = new Date();
        expdate.setTime(expdate.getTime() + (3600 * 1000));
//Set-Cookie: RMID=732423sdfs73242; expires=Fri, 31 Dec 2010 23:59:59 GMT; path=/; domain=.example.net
        appendHeader(Header.KEY_SET_COOKIE, key + "=" + value + "; expires=" + dateFormatThreadLocal.get().format(expdate) + "; path=" + path);
    }

    public Response makeStatic() {
        return new StaticResponse(toReadableBytes());
    }

    private static class StaticResponse extends Response {
        ReadableByteBuffer readable;

        public StaticResponse(ReadableByteBuffer readable) {
            this.readable = readable;
        }

        public StaticResponse(ReadableData readable) {
            ByteBufferWrapper wrapper = new ByteBufferWrapper(readable);
            this.readable = new ReadableByteBuffer(wrapper);
        }

        @Override
        public ReadableData toReadableBytes() {
            return readable.copy();
        }
    }
}

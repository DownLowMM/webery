package com.wizzardo.http;

import com.wizzardo.http.request.Request;
import com.wizzardo.http.response.Response;

import java.io.IOException;

/**
 * @author: wizzardo
 * Date: 07.09.14
 */
public interface Handler extends Named {

    Response handle(Request<HttpConnection, Response> request, Response response) throws IOException;
}

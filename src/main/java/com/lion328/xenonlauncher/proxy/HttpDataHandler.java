package com.lion328.xenonlauncher.proxy;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;

public class HttpDataHandler implements DataHandler {

    public static final HttpRequestHandler STREAM_HANDLER = new HttpRequestHandler() {

        @Override
        public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
            if (httpContext.getAttribute("request.set") != Boolean.TRUE)
                httpContext.setAttribute("response.need-original", true);
        }
    };

    private Map<Integer, HttpRequestHandler> handlers = new TreeMap<>();

    public void addHttpRequestHandler(int level, HttpRequestHandler handler) {
        handlers.put(level, handler);
    }

    @Override
    public boolean process(Socket client, Socket server) throws Exception {
        InputStream clientIn = client.getInputStream();
        clientIn.mark(65536);

        try {
            DefaultBHttpServerConnection httpClient = new DefaultBHttpServerConnection(8192);
            httpClient.bind(client);

            HttpRequest rawRequest = httpClient.receiveRequestHeader();
            HttpEntityEnclosingRequest request;

            if (rawRequest instanceof HttpEntityEnclosingRequest)
                request = (HttpEntityEnclosingRequest) rawRequest;
            else {
                request = new BasicHttpEntityEnclosingRequest(rawRequest.getRequestLine());
                request.setHeaders(rawRequest.getAllHeaders());
            }

            httpClient.receiveRequestEntity(request);

            DefaultBHttpClientConnection httpServer = new DefaultBHttpClientConnection(8192);

            HttpCoreContext context = HttpCoreContext.create();
            HttpResponse response = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));

            context.setAttribute("client.socket", client);
            context.setAttribute("server.socket", server);

            boolean sent = false;

            for (Map.Entry<Integer, HttpRequestHandler> entry : handlers.entrySet()) {
                entry.getValue().handle(request, response, context);

                if (context.getAttribute("response.set") instanceof HttpResponse)
                    response = (HttpResponse) context.getAttribute("response.set");

                if (context.getAttribute("pipeline.end") == Boolean.TRUE)
                    break;

                if (context.getAttribute("response.need-original") == Boolean.TRUE && !sent) {
                    httpServer.bind(server);
                    httpServer.sendRequestHeader(request);
                    httpServer.sendRequestEntity(request);
                    response = httpServer.receiveResponseHeader();
                    httpServer.receiveResponseEntity(response);

                    entry.getValue().handle(request, response, context);

                    context.removeAttribute("response.need-original");
                    context.setAttribute("request.sent", true);

                    sent = true;
                }
            }

            if (context.getAttribute("response.sent") != Boolean.TRUE) {
                httpClient.sendResponseHeader(response);

                if (response.getEntity() != null)
                    httpClient.sendResponseEntity(response);
            }

            return true;
        } catch (ProtocolException e) {
            clientIn.reset();
            return false;
        } catch (ConnectionClosedException e) {
            return true;
        }
    }
}

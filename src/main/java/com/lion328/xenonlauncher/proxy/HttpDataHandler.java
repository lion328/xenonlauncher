package com.lion328.xenonlauncher.proxy;

import org.apache.http.*;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.InputStream;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;

public class HttpDataHandler implements DataHandler {

    private static final String[] allHttpMethod = new String[]{
            "GET", "POST", "CONNECT", "HEAD", "OPTIONS", "TRACE", "PUT", "DELETE"
    };

    private static final int maxHttpMethodLength = 7; // CONNECT (1.0), OPTIONS (1.1)

    private Map<Integer, HttpRequestHandler> handlers = new TreeMap<>();

    public void addHttpRequestHandler(int level, HttpRequestHandler handler) {
        handlers.put(level, handler);
    }

    @Override
    public boolean process(Socket client, Socket server) throws Exception {
        InputStream clientIn = client.getInputStream();
        clientIn.mark(8192);

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
            httpServer.bind(server);
            httpServer.sendRequestHeader(request);
            httpServer.sendRequestEntity(request);
            HttpResponse response = httpServer.receiveResponseHeader();
            httpServer.receiveResponseEntity(response);

            HttpCoreContext context = HttpCoreContext.create();

            for (Map.Entry<Integer, HttpRequestHandler> entry : handlers.entrySet())
                entry.getValue().handle(request, response, context);

            httpClient.sendResponseHeader(response);
            httpClient.sendResponseEntity(response);

            return true;
        } catch (ProtocolException e) {
            clientIn.reset();
            return false;
        } catch (ConnectionClosedException e) {
            return true;
        }
    }
}

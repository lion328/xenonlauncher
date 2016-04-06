package com.lion328.xenonlauncher.main;

import com.lion328.xenonlauncher.proxy.HttpDataHandler;
import com.lion328.xenonlauncher.proxy.ProxyServer;
import com.lion328.xenonlauncher.proxy.StreamDataHandler;
import com.lion328.xenonlauncher.proxy.socks5.SOCKS5ProxyServer;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(35565);
        ProxyServer proxy = new SOCKS5ProxyServer();
        proxy.addDataHandler(Integer.MAX_VALUE, new StreamDataHandler() {

            private int count = 0;

            @Override
            public boolean process(Socket a, Socket b) throws IOException {
                int i = count++;
                System.out.println("Streaming #" + i);
                boolean out = super.process(a, b);
                System.out.println("Disconnected #" + i);
                count--;
                return out;
            }
        });
        HttpDataHandler httpHandler = new HttpDataHandler();
        httpHandler.addHttpRequestHandler(0, new HttpRequestHandler() {

            @Override
            public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
                if (httpContext.getAttribute("send-request") != Boolean.TRUE) {
                    httpContext.setAttribute("need-original", true);
                    return;
                }
                System.out.println("Request to " + httpRequest.getHeaders("Host")[0].getValue() + " " + httpRequest.getRequestLine().getUri());
            }
        });
        proxy.addDataHandler(0, httpHandler);

        System.out.println("Proxy broadcast at " + server.getInetAddress().getHostAddress() + ":" + server.getLocalPort());

        proxy.start(server);
    }
}
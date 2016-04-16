package com.lion328.xenonlauncher.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lion328.xenonlauncher.minecraft.launcher.GameLauncher;
import com.lion328.xenonlauncher.minecraft.launcher.json.JSONGameLauncher;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.DependencyName;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.GameVersion;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.type.DependencyNameTypeAdapter;
import com.lion328.xenonlauncher.proxy.HttpDataHandler;
import com.lion328.xenonlauncher.proxy.ProxyServer;
import com.lion328.xenonlauncher.proxy.StreamDataHandler;
import com.lion328.xenonlauncher.proxy.socks5.SOCKS5ProxyServer;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) throws Exception {
        File basepath = new File("/home/lion328/.minecraft");
        File versions = new File(basepath, "versions");
        File libraries = new File(basepath, "libraries");
        String id = "1.9.2";

        File jsonFile = new File(versions, id + "/" + id + ".json");

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DependencyName.class, new DependencyNameTypeAdapter());
        Gson gson = gsonBuilder.create();
        GameVersion version = gson.fromJson(new FileReader(jsonFile), GameVersion.class);

        GameLauncher launcher = new JSONGameLauncher(version, basepath, libraries, versions);
        final Process process = launcher.launch();

        new Thread() {

            @Override
            public void run() {
                int b;
                try {
                    while ((b = process.getErrorStream().read()) != -1)
                        System.err.write(b);
                } catch (IOException e) {
                    e.printStackTrace();
                    e.printStackTrace();
                }
            }
        }.start();

        Thread td = new Thread() {

            @Override
            public void run() {
                int b;
                try {
                    while ((b = process.getInputStream().read()) != -1)
                        System.out.write(b);
                } catch (IOException e) {
                    e.printStackTrace();
                    e.printStackTrace();
                }
            }
        };
        td.start();
        td.join();

        System.exit(0);

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
                if (httpContext.getAttribute("sent-request") != Boolean.TRUE) {
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
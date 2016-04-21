package com.lion328.xenonlauncher.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lion328.xenonlauncher.downloader.repository.DependencyName;
import com.lion328.xenonlauncher.minecraft.launcher.GameLauncher;
import com.lion328.xenonlauncher.minecraft.launcher.json.JSONGameLauncher;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.GameVersion;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.MergedGameVersion;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.type.DependencyNameTypeAdapter;
import com.lion328.xenonlauncher.minecraft.launcher.patcher.HttpsProtocolPatcher;
import com.lion328.xenonlauncher.proxy.HttpDataHandler;
import com.lion328.xenonlauncher.proxy.ProxyServer;
import com.lion328.xenonlauncher.proxy.StreamDataHandler;
import com.lion328.xenonlauncher.proxy.socks5.SOCKS5ProxyServer;
import com.lion328.xenonlauncher.proxy.util.StreamUtil;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Pattern;

public class Main
{

    public static void main(String[] args) throws Exception
    {

        String s = "http://sessionserver.mojang.com";
        Pattern pattern = Pattern.compile("^https?:\\/\\/(.*\\.)?(mojang\\.com|minecraft\\.net).*");
        System.out.println(pattern.matcher(s).find());

        final ServerSocket server = new ServerSocket(35565);
        final ProxyServer proxy = new SOCKS5ProxyServer();
        proxy.addDataHandler(Integer.MAX_VALUE, new StreamDataHandler()
        {

            private int count = 0;

            @Override
            public boolean process(Socket a, Socket b) throws IOException
            {
                int i = count++;
                System.out.println("Streaming #" + i);
                boolean out = super.process(a, b);
                System.out.println("Disconnected #" + i);
                count--;
                return out;
            }
        });
        HttpDataHandler httpHandler = new HttpDataHandler();
        httpHandler.addHttpRequestHandler(0, new HttpRequestHandler()
        {

            @Override
            public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException
            {
                String host = httpRequest.getHeaders("Host")[0].getValue();
                String uri = httpRequest.getRequestLine().getUri();
                String path = host + uri;
                String completeUri = "http://" + path;

                if (!host.equals("sessionserver.mojang.com"))
                {
                    return;
                }

                final Socket serverConnection = SSLSocketFactory.getDefault().createSocket(host, 443);
                final Socket clientConnection = (Socket) httpContext.getAttribute("client.socket");

                clientConnection.getInputStream().reset();

                System.out.println("Start streaming " + completeUri);

                StreamUtil.pipeStreamThread(clientConnection.getInputStream(), new OutputStream()
                {

                    @Override
                    public void write(int i) throws IOException
                    {
                        serverConnection.getOutputStream().write(i);
                        System.out.write(i);
                    }
                });
                StreamUtil.pipeStream(serverConnection.getInputStream(), new OutputStream()
                {

                    @Override
                    public void write(int i) throws IOException
                    {
                        clientConnection.getOutputStream().write(i);
                        System.out.write(i);
                    }
                });

                System.out.println("Finish streamming " + completeUri);

                httpContext.setAttribute("response.sent", true);
                httpContext.setAttribute("pipeline.end", true);
            }
        });
        httpHandler.addHttpRequestHandler(Integer.MAX_VALUE, HttpDataHandler.STREAM_HANDLER);
        proxy.addDataHandler(50, httpHandler);
        /*proxy.addDataHandler(25, new DataHandler() {

            private final Pattern protocolPattern = Pattern.compile("^(GET)|(POST)|(PUT)|(DELETE)|(CONNECT)|(HEAD)|(OPTIONS)|(TRACE) /[^ ]+ HTTP/\\d(\\.\\d)?$");

            @Override
            public boolean process(Socket client, Socket server) throws Exception {
                InputStream in = client.getInputStream();

                in.mark(65536);

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                List<String> requestLines = new ArrayList<>();
                while ((line = reader.readLine()) != null && !line.equals("\r\n"))
                    requestLines.add(line);

                if (requestLines.size() < 1) {
                    in.reset();
                    return false;
                }

                String firstLine = requestLines.get(0);

                if (!protocolPattern.matcher(firstLine).matches() || !firstLine.endsWith("HTTP/1.1")) {
                    in.reset();
                    return false;
                }

                String host = null;

                for (String requestLine : requestLines) {
                    if (requestLine.toLowerCase().startsWith("host: "))
                        host = requestLine.substring(requestLine.indexOf(':')).trim();
                }

                if (host == null || !host.equalsIgnoreCase("sessionserver.mojang.com")) {
                    in.reset();
                    return false;
                }

                in.reset();

                Socket clientSocket = SSLSocketFactory.getDefault().createSocket(host, 443);

                StreamUtil.pipeStreamThread(in, clientSocket.getOutputStream());
                StreamUtil.pipeStream(clientSocket.getInputStream(), client.getOutputStream());

                return true;
            }
        });*/

        //System.exit(0);

        File basepath = new File("/home/lion328/.minecraft");
        File versions = new File(basepath, "versions");
        File libraries = new File(basepath, "libraries");
        String id = "1.8.9-forge1.8.9-11.15.1.1847";

        File jsonFile = new File(versions, id + "/" + id + ".json");

        System.out.println(jsonFile.getAbsolutePath());

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DependencyName.class, new DependencyNameTypeAdapter());
        Gson gson = gsonBuilder.create();
        GameVersion version = gson.fromJson(new FileReader(jsonFile), GameVersion.class);
        GameVersion parent = gson.fromJson(new FileReader(new File(versions, "1.8.9/1.8.9.json")), GameVersion.class);
        version = new MergedGameVersion(version, parent);

        GameLauncher launcher = new JSONGameLauncher(version, basepath);
        launcher.replaceArgument("auth_player_name", "lion328");
        launcher.replaceArgument("auth_uuid", "f0e9f5b95ce74d3d9545f2013d23ace7");
        launcher.replaceArgument("auth_access_token", "");

        launcher.addJVMArgument("-Dlog4j.configuration=file://log4j.properties");
        launcher.addJVMArgument("-DsocksProxyHost=127.0.0.1");
        launcher.addJVMArgument("-DsocksProxyPort=35565");

        HttpsProtocolPatcher patcher = new HttpsProtocolPatcher("http");
        final DependencyName regex = new DependencyName("com\\.mojang:authlib:.*");
        launcher.addPatcher(regex, patcher);

        final Process process = launcher.launch();

        new Thread()
        {

            @Override
            public void run()
            {
                int b;
                try
                {
                    while ((b = process.getErrorStream().read()) != -1)
                    {
                        System.err.write(b);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    e.printStackTrace();
                }
            }
        }.start();

        Thread td = new Thread()
        {

            @Override
            public void run()
            {
                int b;
                try
                {
                    while ((b = process.getInputStream().read()) != -1)
                    {
                        System.out.write(b);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    e.printStackTrace();
                }
            }
        };
        td.start();
        //td.join();

        //System.exit(0);

        Runtime.getRuntime().addShutdownHook(new Thread()
        {

            @Override
            public void run()
            {
                process.destroy();
                proxy.stop();
            }
        });

        System.out.println("Proxy broadcast at " + server.getInetAddress().getHostAddress() + ":" + server.getLocalPort());

        proxy.start(server);
    }
}
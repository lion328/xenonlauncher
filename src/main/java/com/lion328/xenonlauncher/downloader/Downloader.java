package com.lion328.xenonlauncher.downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Downloader
{

    public static final int BUFFER_SIZE = 8192;

    private final URL inputUrl;
    private final File targetFile;
    private final int bufferSize;
    private final List<Callback> callbackList;

    private boolean running;

    public Downloader(URL url, File file)
    {
        this(url, file, BUFFER_SIZE);
    }

    public Downloader(URL url, File file, int bufferSize)
    {
        inputUrl = url;
        targetFile = file;
        this.bufferSize = bufferSize;
        callbackList = new ArrayList<>();
        running = false;
    }

    public synchronized void download() throws IOException
    {
        if (running)
        {
            return;
        }

        running = true;

        HttpURLConnection connection = (HttpURLConnection) inputUrl.openConnection();
        connection.setRequestMethod("GET");

        int size = connection.getContentLength();
        int downloaded = 0;
        int percentage = 0;

        OutputStream fileOut = new FileOutputStream(targetFile);

        byte[] buffer = new byte[bufferSize];
        int length;

        while (running && ((length = connection.getInputStream().read(buffer)) != -1))
        {
            fileOut.write(buffer, 0, length);
            downloaded += length;

            if ((size != -1) && (percentage < (percentage = downloaded * 100 / size)))
            {
                onPercentageChange(percentage, size, downloaded);
            }
        }

        onPercentageChange(100, size, size);

        fileOut.close();
    }

    public void stop()
    {
        running = false;
    }

    public URL getInputUrl()
    {
        return inputUrl;
    }

    public File getTargetFile()
    {
        return targetFile;
    }

    private void onPercentageChange(int percentage, int size, int downloaded)
    {
        for (Callback callback : callbackList)
        {
            callback.onPercentageChange(percentage, size, downloaded);
        }
    }

    public interface Callback
    {

        void onPercentageChange(int percentage, int size, int downloaded);
    }
}

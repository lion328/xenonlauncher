package com.lion328.xenonlauncher.minecraft.assets.downloader;

import com.lion328.xenonlauncher.downloader.Downloader;
import com.lion328.xenonlauncher.downloader.FileDownloader;
import com.lion328.xenonlauncher.downloader.FileDownloaderCallback;
import com.lion328.xenonlauncher.downloader.URLFileDownloader;
import com.lion328.xenonlauncher.downloader.VerifiyFileDownloader;
import com.lion328.xenonlauncher.downloader.verifier.FileVerifier;
import com.lion328.xenonlauncher.downloader.verifier.MessageDigestFileVerifier;
import com.lion328.xenonlauncher.minecraft.assets.data.AssetsInformation;
import com.lion328.xenonlauncher.minecraft.assets.data.ObjectInformation;
import com.lion328.xenonlauncher.util.URLUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssetsDownloader implements Downloader
{

    public static final URL DEFAULT_ASSETS_URL = URLUtil.constantURL("http://resources.download.minecraft.net/");

    private final AssetsInformation info;
    private final File assetsDir;
    private final URL downloadURL;
    private final int bufferSize;
    private final FileDownloaderCallback callback;
    private final List<FileDownloaderCallback> callbackList;

    private boolean running;
    private File currentFile;
    private int overallPercentage;
    private long size;
    private long downloaded;

    public AssetsDownloader(AssetsInformation info, File assetsDir)
    {
        this(info, assetsDir, DEFAULT_ASSETS_URL);
    }

    public AssetsDownloader(AssetsInformation info, File assetsDir, URL downloadURL)
    {
        this(info, assetsDir, downloadURL, URLFileDownloader.BUFFER_SIZE);
    }

    public AssetsDownloader(AssetsInformation info, File assetsDir, URL downloadURL, int bufferSize)
    {
        this.info = info;
        this.assetsDir = assetsDir;
        this.downloadURL = downloadURL;
        this.bufferSize = bufferSize;

        callback = new FileDownloaderCallback()
        {

            @Override
            public void onPercentageChange(File file, int overallPercentage, long fileSize, long fileDownloaded)
            {
                currentFile = file;
                size = fileSize;
                downloaded = fileDownloaded;

                callCallbacks();
            }
        };

        callbackList = new ArrayList<>();
    }

    private void callCallbacks()
    {
        for (FileDownloaderCallback callback : callbackList)
        {
            callback.onPercentageChange(currentFile, overallPercentage, size, downloaded);
        }
    }

    @Override
    public void download() throws IOException
    {
        if (running)
        {
            return;
        }

        running = true;

        if (info.getObjects() == null)
        {
            currentFile = null;
            overallPercentage = 100;

            callCallbacks();

            return;
        }

        Map<String, ObjectInformation> objects = info.getObjects();
        List<FileDownloader> downloaders = new ArrayList<>();

        for (Map.Entry<String, ObjectInformation> entry : objects.entrySet())
        {
            ObjectInformation object = entry.getValue();

            String path = object.getHash().substring(0, 2) + "/" + object.getHash();
            File file = new File(assetsDir, path);

            if (!file.getParentFile().mkdirs())
            {
                throw new IOException("Can't create file (" + file.getParentFile().toString() + ")");
            }

            URL url = new URL(downloadURL, path);

            FileVerifier verifier = new MessageDigestFileVerifier(MessageDigestFileVerifier.SHA_1, object.getHash());
            FileDownloader downloader = new URLFileDownloader(url, file, bufferSize);
            downloader = new VerifiyFileDownloader(downloader, verifier);

            downloaders.add(downloader);
        }

        int i = 0;
        for (FileDownloader downloader : downloaders)
        {
            overallPercentage = i * 100 / downloaders.size();

            downloader.download();

            i++;
        }

        overallPercentage = 100;
        currentFile = null;

        callCallbacks();

        running = false;
    }

    @Override
    public void stop()
    {
        running = false;
    }

    @Override
    public boolean isRunning()
    {
        return running;
    }

    @Override
    public File getCurrentFile()
    {
        return currentFile;
    }

    @Override
    public int getOverallPercentage()
    {
        return overallPercentage;
    }

    @Override
    public long getCurrentFileSize()
    {
        return size;
    }

    @Override
    public long getCurrentDownloadedSize()
    {
        return downloaded;
    }

    @Override
    public void registerCallback(FileDownloaderCallback callback)
    {
        callbackList.add(callback);
    }

    @Override
    public void removeCallback(FileDownloaderCallback callback)
    {
        callbackList.remove(callback);
    }
}

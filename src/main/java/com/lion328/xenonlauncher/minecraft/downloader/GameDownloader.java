package com.lion328.xenonlauncher.minecraft.downloader;

import com.lion328.xenonlauncher.downloader.DownloaderCallbackHandler;
import com.lion328.xenonlauncher.downloader.FileDownloader;
import com.lion328.xenonlauncher.downloader.FileDownloaderCallback;
import com.lion328.xenonlauncher.downloader.URLFileDownloader;
import com.lion328.xenonlauncher.downloader.VerifiyFileDownloader;
import com.lion328.xenonlauncher.downloader.verifier.MessageDigestFileVerifier;
import com.lion328.xenonlauncher.minecraft.downloader.verifier.MinecraftFileVerifier;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.DownloadInformation;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.GameVersion;
import com.lion328.xenonlauncher.util.URLUtil;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GameDownloader implements DownloaderCallbackHandler
{

    public static final URL DEFAULT_MINECRAFT_VERSIONS = URLUtil.constantURL("https://s3.amazonaws.com/Minecraft.Download/versions/");

    private final GameVersion info;
    private final URL downloadURL;
    private final File basepath;
    private final int bufferSize;
    private final List<FileDownloaderCallback> callbackList;
    private final FileDownloaderCallback callback;

    private boolean running;
    private File currentFile;
    private int overallProgress;
    private long fileSize;
    private long downloadedSize;

    public GameDownloader(GameVersion info, File basepath)
    {
        this(info, DEFAULT_MINECRAFT_VERSIONS, basepath);
    }

    public GameDownloader(GameVersion info, URL downloadURL, File basepath)
    {
        this(info, downloadURL, basepath, URLFileDownloader.BUFFER_SIZE);
    }

    public GameDownloader(GameVersion info, URL downloadURL, File basepath, int bufferSize)
    {
        this.info = info;
        this.downloadURL = downloadURL;
        this.basepath = basepath;
        this.bufferSize = bufferSize;

        callbackList = new ArrayList<>();

        callback = new FileDownloaderCallback()
        {

            @Override
            public void onPercentageChange(File file, int overallPercentage, long size, long fileDownloaded)
            {
                currentFile = file;
                overallProgress = overallPercentage;
                fileSize = size;
                downloadedSize = fileDownloaded;

                callCallbacks();
            }
        };

        running = false;
    }

    public synchronized void download() throws IOException
    {
        if (running)
        {
            return;
        }

        running = true;

        URL jarURL;
        File jarFile = info.getJarFile(basepath);
        FileDownloader downloader;

        if (info.getDownloadsInformation() != null)
        {
            DownloadInformation downloadInfo = info.getDownloadsInformation().get(GameVersion.DOWNLOAD_CLIENT);
            jarURL = downloadInfo.getURL();
            downloader = new URLFileDownloader(jarURL, jarFile, bufferSize);
            downloader = new VerifiyFileDownloader(downloader, new MinecraftFileVerifier(downloadInfo));
        }
        else
        {
            jarURL = new URL(downloadURL, info.getJarName() + "/" + info.getJarName() + ".jar");

            HttpURLConnection connection = (HttpURLConnection) jarURL.openConnection();
            String md5 = connection.getHeaderField("ETag");

            if (md5 == null)
            {
                throw new IOException("ETag header not found");
            }

            // md5 = md5.substring(1, md5.length() - 1); // delete quotes
            md5 = md5.trim().replace("\"", ""); // maybe this is better

            downloader = new URLFileDownloader(jarURL, jarFile, bufferSize);
            downloader = new VerifiyFileDownloader(downloader, new MessageDigestFileVerifier(MessageDigestFileVerifier.MD5, md5));
        }

        downloader.registerCallback(callback);
        downloader.download();

        running = false;
    }

    public void stop()
    {
        running = false;
    }

    private void callCallbacks()
    {
        for (FileDownloaderCallback callback : callbackList)
        {
            callback.onPercentageChange(currentFile, overallProgress, fileSize, downloadedSize);
        }
    }

    @Override
    public File getCurrentFile()
    {
        return currentFile;
    }

    @Override
    public int getOverallPercentage()
    {
        return overallProgress;
    }

    @Override
    public long getCurrentFileSize()
    {
        return fileSize;
    }

    @Override
    public long getCurrentDownloadedSize()
    {
        return downloadedSize;
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

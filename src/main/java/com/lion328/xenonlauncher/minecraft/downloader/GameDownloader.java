package com.lion328.xenonlauncher.minecraft.downloader;

import com.lion328.xenonlauncher.downloader.Downloader;
import com.lion328.xenonlauncher.downloader.DownloaderCallbackHandler;
import com.lion328.xenonlauncher.downloader.FileDownloader;
import com.lion328.xenonlauncher.downloader.FileDownloaderCallback;
import com.lion328.xenonlauncher.downloader.URLFileDownloader;
import com.lion328.xenonlauncher.downloader.VerifiyFileDownloader;
import com.lion328.xenonlauncher.downloader.repository.Repository;
import com.lion328.xenonlauncher.downloader.verifier.MessageDigestFileVerifier;
import com.lion328.xenonlauncher.minecraft.downloader.verifier.MinecraftFileVerifier;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.DownloadInformation;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.GameLibrary;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.GameVersion;
import com.lion328.xenonlauncher.util.OS;
import com.lion328.xenonlauncher.util.URLUtil;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GameDownloader implements DownloaderCallbackHandler
{

    public static final URL MINECRAFT_VERSIONS = URLUtil.constantURL("https://s3.amazonaws.com/Minecraft.Download/versions/");

    private final GameVersion info;
    private final File basepath;
    private final Repository defaultRepository;
    private final OS os;
    private final String osVersion;
    private final String osArch;
    private final int bufferSize;
    private final List<FileDownloaderCallback> callbackList;
    private final FileDownloaderCallback callback;

    private boolean running;
    private File currentFile;
    private int overallProgress;
    private long fileSize;
    private long downloadedSize;

    public GameDownloader(GameVersion info, File basepath, Repository defaultRepository)
    {
        this(info, basepath, defaultRepository, OS.getCurrentOS(), OS.getCurrentVersion(), OS.getCurrentArchitecture());
    }

    public GameDownloader(GameVersion info, File basepath, Repository defaultRepository, OS os, String osVersion, String osArch)
    {
        this(info, basepath, defaultRepository, os, osVersion, osArch, URLFileDownloader.BUFFER_SIZE);
    }

    public GameDownloader(GameVersion info, File basepath, Repository defaultRepository, OS os, String osVersion, String osArch, int bufferSize)
    {
        this.info = info;
        this.basepath = basepath;
        this.defaultRepository = defaultRepository;
        this.os = os;
        this.osVersion = osVersion;
        this.osArch = osArch;
        this.bufferSize = bufferSize;

        callbackList = new ArrayList<>();

        callback = new FileDownloaderCallback()
        {

            @Override
            public void onPercentageChange(File file, int overallPercentage, long size, long fileDownloaded)
            {
                currentFile = file;
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

        List<Downloader> downloaders = new ArrayList<>();
        URL jarURL = null;
        File jarFile = info.getJarFile(basepath);
        FileDownloader downloader;

        if (info.getDownloadsInformation() != null)
        {
            DownloadInformation downloadInfo = info.getDownloadsInformation().get(GameVersion.DOWNLOAD_CLIENT);
            jarURL = downloadInfo.getURL();
            downloader = new URLFileDownloader(jarURL, info.getJarFile(basepath), bufferSize);
            downloader = new VerifiyFileDownloader(downloader, new MinecraftFileVerifier(downloadInfo));
        }
        else
        {
            jarURL = new URL(MINECRAFT_VERSIONS, info.getJarName() + "/" + info.getJarName() + ".jar");

            HttpURLConnection connection = (HttpURLConnection) jarURL.openConnection();
            String md5 = connection.getHeaderField("ETag");

            if (md5 == null)
            {
                throw new IOException("ETag header not found");
            }

            // md5 = md5.substring(1, md5.length() - 1); // delete quotes
            md5 = md5.trim().replace("\"", ""); // maybe this is better

            downloader = new URLFileDownloader(jarURL, info.getJarFile(basepath), bufferSize);
            downloader = new VerifiyFileDownloader(downloader, new MessageDigestFileVerifier(MessageDigestFileVerifier.MD5, md5));
        }

        downloaders.add(downloader);

        for (GameLibrary library : info.getLibraries())
        {
            downloaders.add(new LibraryDownloader(library, basepath, os, osArch, defaultRepository, bufferSize));
        }

        int i = 0;
        for (Downloader dl : downloaders)
        {
            overallProgress = i * 100 / downloaders.size();

            dl.registerCallback(callback);
            dl.download();

            i++;
        }

        overallProgress = 100;

        callCallbacks();

        running = false;
    }

    public void stop()
    {
        running = false;
    }

    public Repository getDefaultRepository()
    {
        return defaultRepository;
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

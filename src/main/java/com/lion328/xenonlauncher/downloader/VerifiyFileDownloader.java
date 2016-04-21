package com.lion328.xenonlauncher.downloader;

import com.lion328.xenonlauncher.downloader.verifier.FileVerifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VerifiyFileDownloader implements FileDownloader
{

    private final FileDownloader downloader;
    private final FileVerifier verifier;
    private final List<FileDownloaderCallback> callbackList;
    private final FileDownloaderCallback callback;

    private int percentage;
    private long size;
    private long downloaded;

    public VerifiyFileDownloader(FileDownloader downloader, FileVerifier verifier)
    {
        this.downloader = downloader;
        this.verifier = verifier;

        callback = new FileDownloaderCallback()
        {

            @Override
            public void onPercentageChange(File file, int overallPercentage, long fileSize, long fileDownloaded)
            {
                for (FileDownloaderCallback callback : callbackList)
                {
                    callback.onPercentageChange(file, overallPercentage, fileSize, fileDownloaded);
                }
            }
        };

        callbackList = new ArrayList<>();
        callbackList.add(callback);

        reset();
    }

    private void reset()
    {
        percentage = 0;
        size = -1;
        downloaded = -1;
    }

    private void runCallback()
    {
        callback.onPercentageChange(downloader.getFile(), percentage, size, downloaded);
    }

    @Override
    public synchronized void download() throws IOException
    {
        if (downloader.isRunning())
        {
            return;
        }

        reset();
        runCallback();


        if (verifier.isValid(getFile()))
        {
            return;
        }

        downloader.download();
    }

    @Override
    public void stop()
    {
        downloader.stop();
    }

    @Override
    public boolean isRunning()
    {
        return downloader.isRunning();
    }

    @Override
    public File getCurrentFile()
    {
        return downloader.getCurrentFile();
    }

    @Override
    public int getOverallPercentage()
    {
        return downloader.getOverallPercentage();
    }

    @Override
    public long getCurrentFileSize()
    {
        return downloader.getCurrentFileSize();
    }

    @Override
    public long getCurrentDownloadedSize()
    {
        return downloader.getCurrentDownloadedSize();
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

    @Override
    public File getFile()
    {
        return downloader.getFile();
    }
}

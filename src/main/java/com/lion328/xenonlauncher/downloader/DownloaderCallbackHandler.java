package com.lion328.xenonlauncher.downloader;

import java.io.File;

public interface DownloaderCallbackHandler
{

    File getCurrentFile();

    int getOverallPercentage();

    long getCurrentFileSize();

    long getCurrentDownloadedSize();

    void registerCallback(FileDownloaderCallback callback);

    void removeCallback(FileDownloaderCallback callback);
}

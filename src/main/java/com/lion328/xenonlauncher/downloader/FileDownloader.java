package com.lion328.xenonlauncher.downloader;

import java.io.File;
import java.io.IOException;

public interface FileDownloader extends DownloaderCallbackHandler
{

    boolean download() throws IOException;

    void stop();

    boolean isRunning();

    File getFile();
}

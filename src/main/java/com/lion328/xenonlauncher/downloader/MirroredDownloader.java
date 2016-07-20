/*
 * Copyright (c) 2016 Waritnan Sookbuntherng
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.lion328.xenonlauncher.downloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MirroredDownloader implements Downloader
{

    private final Downloader main;
    private final Downloader mirror;
    private final List<DownloaderCallback> callbackList;

    public MirroredDownloader(Downloader main, Downloader mirror)
    {
        this.main = main;
        this.mirror = mirror;
        this.callbackList = new ArrayList<>();

        DownloaderCallback callback = new DownloaderCallback()
        {

            @Override
            public void onPercentageChange(File file, int overallPercentage, long fileSize, long fileDownloaded)
            {
                for (DownloaderCallback callback : callbackList)
                {
                    onPercentageChange(file, overallPercentage, fileSize, fileDownloaded);
                }
            }
        };

        main.registerCallback(callback);
        mirror.registerCallback(callback);
    }

    @Override
    public void download() throws IOException
    {
        try
        {
            main.download();
        }
        catch (IOException e)
        {
            mirror.download();
        }
    }

    @Override
    public void stop()
    {
        main.stop();
        mirror.stop();
    }

    @Override
    public boolean isRunning()
    {
        return main.isRunning() || mirror.isRunning();
    }

    @Override
    public File getCurrentFile()
    {
        if (main.getCurrentFile() != null)
        {
            return main.getCurrentFile();
        }

        return mirror.getCurrentFile();
    }

    @Override
    public int getOverallPercentage()
    {
        if (main.getOverallPercentage() >= mirror.getOverallPercentage())
        {
            return main.getOverallPercentage();
        }

        return mirror.getOverallPercentage();
    }

    @Override
    public long getCurrentFileSize()
    {
        if (main.getCurrentFileSize() >= mirror.getCurrentFileSize())
        {
            return main.getCurrentFileSize();
        }

        return mirror.getCurrentFileSize();
    }

    @Override
    public long getCurrentDownloadedSize()
    {
        if (main.getCurrentDownloadedSize() >= mirror.getCurrentDownloadedSize())
        {
            return main.getCurrentDownloadedSize();
        }

        return mirror.getCurrentDownloadedSize();
    }

    @Override
    public void registerCallback(DownloaderCallback callback)
    {
        callbackList.add(callback);
    }

    @Override
    public void removeCallback(DownloaderCallback callback)
    {
        callbackList.remove(callback);
    }
}

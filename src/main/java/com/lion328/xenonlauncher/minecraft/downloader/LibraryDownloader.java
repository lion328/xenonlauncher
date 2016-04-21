package com.lion328.xenonlauncher.minecraft.downloader;

import com.lion328.xenonlauncher.downloader.Downloader;
import com.lion328.xenonlauncher.downloader.FileDownloader;
import com.lion328.xenonlauncher.downloader.FileDownloaderCallback;
import com.lion328.xenonlauncher.downloader.URLFileDownloader;
import com.lion328.xenonlauncher.downloader.VerifiyFileDownloader;
import com.lion328.xenonlauncher.downloader.repository.DependencyName;
import com.lion328.xenonlauncher.downloader.repository.Repository;
import com.lion328.xenonlauncher.downloader.verifier.FileVerifier;
import com.lion328.xenonlauncher.downloader.verifier.MessageDigestFileVerifier;
import com.lion328.xenonlauncher.downloader.verifier.RepositoryFileVerifier;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.DownloadInformation;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.GameLibrary;
import com.lion328.xenonlauncher.util.OS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LibraryDownloader implements Downloader
{

    public static final int RETRIES = 5;

    private final GameLibrary library;
    private final File basepathDir;
    private final File librariesDir;
    private final OS os;
    private final String arch;
    private final Repository defaultRepository;
    private final List<FileDownloaderCallback> callbackList;
    private final FileDownloaderCallback callback;

    private boolean running;
    private File currentFile;
    private int percentage;
    private long size;
    private long downloaded;

    public LibraryDownloader(GameLibrary library, File basepathDir, Repository defaultRepository)
    {
        this(library, basepathDir, OS.getCurrentOS(), OS.getCurrentArchitecture(), defaultRepository);
    }

    public LibraryDownloader(GameLibrary library, File basepathDir, OS os, String arch, Repository defaultRepository)
    {
        this(library, basepathDir, os, arch, defaultRepository, URLFileDownloader.BUFFER_SIZE);
    }

    public LibraryDownloader(GameLibrary library, File basepathDir, OS os, String arch, Repository defaultRepository, int bufferSize)
    {
        this.library = library;
        this.basepathDir = basepathDir;
        this.librariesDir = new File(basepathDir, "libraries");
        this.os = os;
        this.arch = arch;
        this.defaultRepository = defaultRepository;
        callbackList = new ArrayList<>();

        callback = new FileDownloaderCallback()
        {

            @Override
            public void onPercentageChange(File file, int overallPercentage, long fileSize, long fileDownloaded)
            {
                size = fileSize;
                downloaded = fileDownloaded;
                onPercentageChange(file, percentage, fileSize, fileDownloaded);
            }
        };

        this.running = false;
    }

    @Override
    public synchronized void download() throws IOException
    {
        if (running)
        {
            return;
        }

        running = true;

        List<FileDownloader> downloaders = new ArrayList<>();
        File file;

        if (library.isJavaLibrary())
        {
            if (library.getDownloadInfo().getArtifactInfo().getPath() == null)
            {
                file = library.getDependencyName().getFile(librariesDir);
            }
            else
            {
                file = new File(librariesDir, library.getDownloadInfo().getArtifactInfo().getPath());
            }

            downloaders.add(getDownloader(file, null));
        }

        if (library.isNativesLibrary())
        {
            String classifier = library.getNatives().getNative(os, arch);

            if (library.getDownloadInfo().getArtifactInfo().getPath() == null)
            {
                file = library.getDependencyName().getFile(librariesDir, classifier);
            }
            else
            {
                file = new File(librariesDir, library.getDownloadInfo().getClassfiersInfo().get(classifier).getPath());
            }

            downloaders.add(getDownloader(file, classifier));
        }

        int i = 0;
        for (FileDownloader downloader : downloaders)
        {
            percentage = i * 100 / downloaders.size();

            downloader.registerCallback(callback);

            for (int j = 0; true; j++)
            {
                try
                {
                    downloader.download();
                    break;
                }
                catch (IOException e)
                {
                    if (j >= RETRIES)
                    {
                        throw e;
                    }
                }
            }

            i++;
        }

        running = false;
    }

    private FileDownloader getDownloader(File file, String classifier) throws IOException
    {
        FileDownloader downloader;
        FileVerifier verifier;

        if (library.getDownloadInfo() != null)
        {
            DownloadInformation downloadInfo;

            if (classifier == null)
            {
                downloadInfo = library.getDownloadInfo().getArtifactInfo();
            }
            else
            {
                downloadInfo = library.getDownloadInfo().getClassfiersInfo().get(classifier);
            }

            downloader = new URLFileDownloader(downloadInfo.getURL(), file);
            verifier = new MinecraftFileVerifier(downloadInfo);
        }
        else
        {
            DependencyName name = library.getDependencyName();
            verifier = new RepositoryFileVerifier(defaultRepository, name, classifier, MessageDigestFileVerifier.SHA_1);
            downloader = defaultRepository.getDownloader(name, classifier, null, file);
        }

        return new VerifiyFileDownloader(downloader, verifier);
    }

    public void stop()
    {
        running = false;
    }

    public boolean isRunning()
    {
        return running;
    }

    private void onPercentageChange(File file, int percentage, int size, int downloaded)
    {
        for (FileDownloaderCallback callback : callbackList)
        {
            callback.onPercentageChange(file, percentage, size, downloaded);
        }
    }

    public Repository getDefaultRepository()
    {
        return defaultRepository;
    }

    @Override
    public File getCurrentFile()
    {
        return currentFile;
    }

    @Override
    public int getOverallPercentage()
    {
        return percentage;
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

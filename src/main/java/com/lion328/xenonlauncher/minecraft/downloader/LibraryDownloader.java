package com.lion328.xenonlauncher.minecraft.downloader;

import com.lion328.xenonlauncher.downloader.DownloaderCallbackHandler;
import com.lion328.xenonlauncher.downloader.FileDownloader;
import com.lion328.xenonlauncher.downloader.FileDownloaderCallback;
import com.lion328.xenonlauncher.downloader.URLFileDownloader;
import com.lion328.xenonlauncher.downloader.repository.DependencyName;
import com.lion328.xenonlauncher.downloader.repository.MirroredRepository;
import com.lion328.xenonlauncher.downloader.repository.Repository;
import com.lion328.xenonlauncher.downloader.verifier.FileVerifier;
import com.lion328.xenonlauncher.downloader.verifier.MessageDigestFileVerifier;
import com.lion328.xenonlauncher.downloader.verifier.RepositoryFileVerifier;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.DownloadInformation;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.GameLibrary;
import com.lion328.xenonlauncher.util.OS;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LibraryDownloader implements DownloaderCallbackHandler
{

    public static final int RETRIES = 5;

    private final GameLibrary library;
    private final File basepathDir;
    private final File librariesDir;
    private final OS os;
    private final String arch;
    private final List<Repository> defaultRepository;
    private final List<FileDownloaderCallback> callbackList;
    private final FileDownloaderCallback callback;

    private boolean running;
    private File currentFile;
    private int percentage;
    private long size;
    private long downloaded;

    public LibraryDownloader(GameLibrary library, File basepathDir, List<Repository> defaultRepository)
    {
        this(library, basepathDir, OS.getCurrentOS(), OS.getCurrentArchitecture(), defaultRepository);
    }

    public LibraryDownloader(GameLibrary library, File basepathDir, OS os, String arch, List<Repository> defaultRepository)
    {
        this(library, basepathDir, os, arch, defaultRepository, URLFileDownloader.BUFFER_SIZE);
    }

    public LibraryDownloader(GameLibrary library, File basepathDir, OS os, String arch, List<Repository> defaultRepository, int bufferSize)
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
                onPercentageChange(file, percentage, fileSize, fileDownloaded);
            }
        };

        this.running = false;
    }

    public synchronized void download() throws IOException
    {
        if (running)
        {
            return;
        }

        running = true;

        Map<FileDownloader, FileVerifier> downloaders = new HashMap<>();
        File file;

        if (library.isJavaLibrary())
        {
            file = library.getDependencyName().getFile(librariesDir);
            downloaders.entrySet().add(getDownloaderAndVerifier(file, null));
        }

        if (library.isNativesLibrary())
        {
            String classifier = library.getNatives().getNative(os, arch);
            file = library.getDependencyName().getFile(librariesDir, classifier);
            downloaders.entrySet().add(getDownloaderAndVerifier(file, classifier));
        }

        int i = 0;
        for (Map.Entry<FileDownloader, FileVerifier> entry : downloaders.entrySet())
        {
            percentage = i * 100 / downloaders.size();

            currentFile = entry.getKey().getFile();
            FileVerifier verifier = entry.getValue();

            if (verifier.isValid(currentFile))
            {
                continue;
            }

            FileDownloader downloader = entry.getKey();

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

    private Map.Entry<FileDownloader, FileVerifier> getDownloaderAndVerifier(File file, String classifier) throws IOException
    {
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

            FileDownloader downloader = new URLFileDownloader(downloadInfo.getURL(), file);
            FileVerifier verifier = new MinecraftFileVerifier(downloadInfo);

            return new AbstractMap.SimpleEntry<>(downloader, verifier);
        }

        Repository repository = getRepository();

        if (repository == null)
        {
            return null;
        }

        DependencyName name = library.getDependencyName();
        FileVerifier verifier = new RepositoryFileVerifier(repository, name, classifier, MessageDigestFileVerifier.SHA_1);
        FileDownloader downloader = repository.getDownloader(name, classifier, null, file);

        return new AbstractMap.SimpleEntry<>(downloader, verifier);
    }

    private Repository getRepository()
    {
        if (defaultRepository.size() < 1)
        {
            return null;
        }

        DependencyName name = library.getDependencyName();
        Iterator<Repository> iterator = defaultRepository.iterator();
        Repository repository = iterator.next();

        while (iterator.hasNext())
        {
            repository = new MirroredRepository(repository, iterator.next());
        }

        return repository;
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

    public List<Repository> getDefaultRepositories()
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

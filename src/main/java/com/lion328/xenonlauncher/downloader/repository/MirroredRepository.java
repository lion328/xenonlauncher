package com.lion328.xenonlauncher.downloader.repository;

import com.lion328.xenonlauncher.downloader.FileDownloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MirroredRepository implements Repository
{

    private final Repository main;
    private final Repository mirror;

    public MirroredRepository(Repository main, Repository mirror)
    {
        this.main = main;
        this.mirror = mirror;
    }

    public Repository getMainRepository()
    {
        return main;
    }

    public Repository getMirrorRepository()
    {
        return mirror;
    }

    @Override
    public FileDownloader getDownloader(DependencyName name, String classifier, String extension, File targetFile) throws IOException
    {
        try
        {
            return main.getDownloader(name, classifier, extension, targetFile);
        }
        catch (IOException e)
        {
            return mirror.getDownloader(name, classifier, extension, targetFile);
        }
    }

    @Override
    public InputStream getInputStream(DependencyName name, String classifier, String extension) throws IOException
    {
        try
        {
            return main.getInputStream(name, classifier, extension);
        }
        catch (IOException e)
        {
            return mirror.getInputStream(name, classifier, extension);
        }
    }
}

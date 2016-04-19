package com.lion328.xenonlauncher.minecraft.downloader;

import com.lion328.xenonlauncher.downloader.repository.MavenRepository;
import com.lion328.xenonlauncher.downloader.repository.Repository;
import com.lion328.xenonlauncher.util.URLUtil;

import java.util.ArrayList;
import java.util.List;

public class Repositories
{

    public static final List<Repository> DEFAULT_REPOSITORY;

    public static final MavenRepository MINECRAFT_REPOSITORY;
    public static final MavenRepository MAVENCENTRAL_REPOSITORY;

    static
    {
        DEFAULT_REPOSITORY = new ArrayList<>();
        MINECRAFT_REPOSITORY = new MavenRepository(URLUtil.constantURL("https://libraries.minecraft.net/"));
        MAVENCENTRAL_REPOSITORY = new MavenRepository(URLUtil.constantURL("http://repo1.maven.org/maven2/"));

        DEFAULT_REPOSITORY.add(MINECRAFT_REPOSITORY);
        //DEFAULT_REPOSITORY.add(MAVENCENTRAL_REPOSITORY);
    }
}

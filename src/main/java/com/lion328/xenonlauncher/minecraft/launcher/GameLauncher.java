package com.lion328.xenonlauncher.minecraft.launcher;

import com.lion328.xenonlauncher.downloader.repository.DependencyName;
import com.lion328.xenonlauncher.minecraft.launcher.patcher.LibraryPatcher;

public interface GameLauncher
{

    Process launch() throws Exception;

    void addPatcher(DependencyName regex, LibraryPatcher patcher);

    void replaceArgument(String key, String value);

    void addJVMArgument(String arg);

    void addGameArgument(String key, String value);

    void addGameArgument(String arg);

    void setMaxMemorySize(int mb);
}

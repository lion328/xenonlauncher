package com.lion328.xenonlauncher.minecraft.launcher.patcher;

public interface LibraryPatcher
{

    byte[] patchFile(String path, byte[] original) throws Exception;
}

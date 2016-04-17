package com.lion328.xenonlauncher.minecraft.launcher.patcher;

public interface LibraryPatcher
{

    byte[] patchClass(String name, byte[] original) throws Exception;
}

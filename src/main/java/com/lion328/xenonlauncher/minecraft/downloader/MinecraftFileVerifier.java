package com.lion328.xenonlauncher.minecraft.downloader;

import com.lion328.xenonlauncher.downloader.verifier.MessageDigestFileVerifier;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.DownloadInformation;

public class MinecraftFileVerifier extends MessageDigestFileVerifier
{

    public MinecraftFileVerifier(DownloadInformation info)
    {
        super(MessageDigestFileVerifier.SHA_1, info.getSHA1Hash());
    }
}

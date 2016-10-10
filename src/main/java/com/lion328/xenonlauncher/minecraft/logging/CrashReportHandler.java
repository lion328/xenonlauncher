package com.lion328.xenonlauncher.minecraft.logging;

import java.io.File;

public interface CrashReportHandler
{

    void onGameCrash(File crashReportFile);
}

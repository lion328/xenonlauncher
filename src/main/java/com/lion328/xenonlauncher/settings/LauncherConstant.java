package com.lion328.xenonlauncher.settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LauncherConstant
{

    public static final Logger LOGGER;
    public static final String VERSION;

    static
    {
        LOGGER = LogManager.getLogger("XenonLauncher");
        VERSION = "0.1";
    }
}

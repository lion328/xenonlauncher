package com.lion328.xenonlauncher.settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LauncherConstant
{

    public static final String VERSION = "0.1";
    public static final String ID;

    static
    {
        InputStream in = LauncherConstant.class.getResourceAsStream("/com/lion328/xenonlauncher/settings/launcherid");
        String id = null;

        try
        {
            id = new BufferedReader(new InputStreamReader(in)).readLine().trim();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        ID = id;
    }
}

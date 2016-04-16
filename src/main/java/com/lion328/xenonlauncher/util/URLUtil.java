package com.lion328.xenonlauncher.util;

import java.net.MalformedURLException;
import java.net.URL;

public class URLUtil {

    public static URL safeURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

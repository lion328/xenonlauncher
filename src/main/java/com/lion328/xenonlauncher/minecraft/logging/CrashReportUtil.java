package com.lion328.xenonlauncher.minecraft.logging;

import java.util.regex.Pattern;

public class CrashReportUtil
{

    public static final Pattern CRASH_REPORT_REGEX = Pattern.compile("^.*#@!@#.*Crash report saved to: #@!@# (.*)$");
}

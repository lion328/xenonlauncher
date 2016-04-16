package com.lion328.xenonlauncher.minecraft.launcher.json.data;

public class DependencyName {

    private final String packageName;
    private final String name;
    private final String version;

    public DependencyName(String packageName, String name, String version) {
        this.packageName = packageName;
        this.name = name;
        this.version = version;
    }

    public DependencyName(String shortName) {
        String[] list = shortName.split(":");

        if (list.length < 2)
            throw new IllegalArgumentException("Invalid dependency short name");

        packageName = list[0];
        name = list[1];
        version = list[2];
    }

    public String getShortName() {
        return packageName + ":" + name + ":" + version;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String toString() {
        return getShortName();
    }
}

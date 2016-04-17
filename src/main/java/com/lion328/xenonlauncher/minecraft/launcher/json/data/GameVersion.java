package com.lion328.xenonlauncher.minecraft.launcher.json.data;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class GameVersion
{

    public static final int PARSER_VERSION = 18;

    @SerializedName("id")
    private String id;
    @SerializedName("time")
    private Date time;
    @SerializedName("releaseTime")
    private Date releaseTime;
    @SerializedName("type")
    private ReleaseType releaseType;
    @SerializedName("minecraftArguments")
    private String minecraftArguments;
    @SerializedName("libraries")
    private List<GameLibrary> libraries;
    @SerializedName("mainClass")
    private String mainClass;
    @SerializedName("minimumLauncherVersion")
    private int version;
    @SerializedName("assets")
    private String assets = "legacy";
    @SerializedName("downloads")
    private Map<String, DownloadInformation> downloads;
    @SerializedName("assetInfo")
    private AssetInformation assetInformation;
    @SerializedName("inheritsFrom")
    private String parentId;

    public GameVersion()
    {

    }

    public GameVersion(String id, Date time, Date releaseTime, ReleaseType releaseType, String minecraftArguments, List<GameLibrary> libraries, String mainClass, int version, String assets, Map<String, DownloadInformation> downloads, AssetInformation assetInformation, String parentId)
    {
        this.id = id;
        this.time = time;
        this.releaseTime = releaseTime;
        this.releaseType = releaseType;
        this.minecraftArguments = minecraftArguments;
        this.libraries = libraries;
        this.mainClass = mainClass;
        this.version = version;
        this.assets = assets;
        this.downloads = downloads;
        this.assetInformation = assetInformation;
        this.parentId = parentId;
    }

    public String getID()
    {
        return id;
    }

    public Date getTime()
    {
        return time;
    }

    public Date getReleaseTime()
    {
        return releaseTime;
    }

    public ReleaseType getReleaseType()
    {
        return releaseType;
    }

    public String getMinecraftArguments()
    {
        return minecraftArguments;
    }

    public List<GameLibrary> getLibraries()
    {
        return libraries;
    }

    public String getMainClass()
    {
        return mainClass;
    }

    public int getMinimumLauncherVersion()
    {
        return version;
    }

    public String getAssets()
    {
        return assets;
    }

    public Map<String, DownloadInformation> getDownloadsInformation()
    {
        return downloads;
    }

    public AssetInformation getAssetInformation()
    {
        return assetInformation;
    }

    public String getParentID()
    {
        return parentId;
    }
}

package com.lion328.xenonlauncher.minecraft.launcher.json.data;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class LibraryDownloadInfomation
{

    @SerializedName("artifact")
    private DownloadInformation artifactInfo;
    @SerializedName("classifiers")
    private Map<String, DownloadInformation> classfiersInfo;

    public LibraryDownloadInfomation()
    {

    }

    public LibraryDownloadInfomation(DownloadInformation artifact, Map<String, DownloadInformation> classfiers)
    {
        artifactInfo = artifact;
        classfiersInfo = classfiers;
    }

    public DownloadInformation getArtifactInfo()
    {
        return artifactInfo;
    }

    public Map<String, DownloadInformation> getClassfiersInfo()
    {
        return classfiersInfo;
    }
}

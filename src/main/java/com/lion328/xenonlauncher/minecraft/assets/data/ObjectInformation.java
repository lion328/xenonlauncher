package com.lion328.xenonlauncher.minecraft.assets.data;

import com.google.gson.annotations.SerializedName;

public class ObjectInformation
{

    @SerializedName("hash")
    private String hash;
    @SerializedName("size")
    private long size;

    public ObjectInformation()
    {

    }

    public ObjectInformation(String hash, long size)
    {
        this.hash = hash;
        this.size = size;
    }

    public String getHash()
    {
        return hash;
    }

    public long getSize()
    {
        return size;
    }
}

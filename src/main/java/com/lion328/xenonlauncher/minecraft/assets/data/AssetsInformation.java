package com.lion328.xenonlauncher.minecraft.assets.data;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class AssetsInformation
{

    @SerializedName("objects")
    private Map<String, ObjectInformation> objects;

    public AssetsInformation()
    {

    }

    public AssetsInformation(Map<String, ObjectInformation> objects)
    {
        this.objects = objects;
    }

    public Map<String, ObjectInformation> getObjects()
    {
        return objects;
    }
}

package com.lion328.xenonlauncher.minecraft.api.authentication.yggdrasil.message.response;

import com.google.gson.annotations.SerializedName;
import com.lion328.xenonlauncher.minecraft.api.authentication.yggdrasil.UserProfile;

import java.util.List;

public class AuthenticateResponse
{

    @SerializedName("accessToken")
    private String accessToken;
    @SerializedName("clientToken")
    private String clientToken;
    @SerializedName("availableProfiles")
    private List<UserProfile> availableProfiles;
    @SerializedName("selectedProfile")
    private UserProfile profile;

    public AuthenticateResponse()
    {

    }

    public AuthenticateResponse(String accessToken, String clientToken, List<UserProfile> availableProfiles, UserProfile profile)
    {
        this.accessToken = accessToken;
        this.clientToken = clientToken;
        this.availableProfiles = availableProfiles;
        this.profile = profile;
    }

    public String getAccessToken()
    {
        return accessToken;
    }

    public String getClientToken()
    {
        return clientToken;
    }

    public List<UserProfile> getAvailableProfiles()
    {
        return availableProfiles;
    }

    public UserProfile getSelectedProfile()
    {
        return profile;
    }
}

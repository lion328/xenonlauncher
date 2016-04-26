package com.lion328.xenonlauncher.minecraft.api.authentication.yggdrasil.message.request;

import com.google.gson.annotations.SerializedName;
import com.lion328.xenonlauncher.minecraft.api.authentication.yggdrasil.GameAgent;

public class AuthenticateRequest
{

    @SerializedName("agent")
    private GameAgent agent;
    @SerializedName("username")
    private String username;
    @SerializedName("password")
    private String password;
    @SerializedName("clientToken")
    private String clientToken;

    public AuthenticateRequest()
    {

    }

    public AuthenticateRequest(GameAgent agent, String username, String password, String clientToken)
    {
        this.agent = agent;
        this.username = username;
        this.password = password;
        this.clientToken = clientToken;
    }

    public GameAgent getAgent()
    {
        return agent;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getClientToken()
    {
        return clientToken;
    }
}

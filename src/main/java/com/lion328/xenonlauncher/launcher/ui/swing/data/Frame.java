package com.lion328.xenonlauncher.launcher.ui.swing.data;

import com.google.gson.annotations.SerializedName;
import com.lion328.xenonlauncher.launcher.ui.swing.data.type.ComponentTypeAdapter;

public class Frame extends Component
{

    public static final String TYPE = "frame";

    static
    {
        ComponentTypeAdapter.registerComponent(TYPE, Frame.class);
    }

    @SerializedName("resizable")
    private boolean resizable = false;
    @SerializedName("decorated")
    private boolean decorated = true;
    @SerializedName("panel")
    private Panel panel;

    public Frame(String id, int width, int height, int x, int y, int color, boolean resizable, boolean decorated)
    {
        this(id, TYPE, width, height, x, y, color, resizable, decorated);
    }

    public Frame(String id, String type, int width, int height, int x, int y, int color, boolean resizable, boolean decorated)
    {
        super(id, type, width, height, x, y, color);

        this.resizable = resizable;
        this.decorated = decorated;
    }

    public boolean isResizable()
    {
        return resizable;
    }

    public boolean isDecorated()
    {
        return decorated;
    }

    public Panel getPanel()
    {
        return panel;
    }
}

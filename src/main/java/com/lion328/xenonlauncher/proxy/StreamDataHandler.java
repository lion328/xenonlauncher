package com.lion328.xenonlauncher.proxy;

import com.lion328.xenonlauncher.proxy.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamDataHandler implements DataHandler {

    @Override
    public boolean process(InputStream inA, OutputStream outA, InputStream inB, OutputStream outB) throws IOException {
        StreamUtil.pipeStreamThread(inA, outB);
        StreamUtil.pipeStream(inB, outA);
        return true;
    }
}

package com.lion328.xenonlauncher.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface DataHandler {

    boolean process(InputStream inA, OutputStream outA, InputStream inB, OutputStream outB) throws IOException;
}

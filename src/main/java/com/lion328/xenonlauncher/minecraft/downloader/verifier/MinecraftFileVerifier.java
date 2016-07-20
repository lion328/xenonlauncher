/*
 * Copyright (c) 2016 Waritnan Sookbuntherng
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.lion328.xenonlauncher.minecraft.downloader.verifier;

import com.lion328.xenonlauncher.downloader.repository.DependencyName;
import com.lion328.xenonlauncher.downloader.repository.Repository;
import com.lion328.xenonlauncher.downloader.verifier.FileVerifier;
import com.lion328.xenonlauncher.downloader.verifier.MergedFileVerifier;
import com.lion328.xenonlauncher.downloader.verifier.MessageDigestFileVerifier;
import com.lion328.xenonlauncher.downloader.verifier.RepositoryFileVerifier;
import com.lion328.xenonlauncher.downloader.verifier.SizeFileVerifier;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.DownloadInformation;

import java.io.File;
import java.io.IOException;

public class MinecraftFileVerifier implements FileVerifier
{

    private final FileVerifier verifier;

    public MinecraftFileVerifier(Repository repo, DependencyName name, String classifier) throws IOException
    {
        verifier = new RepositoryFileVerifier(repo, name, classifier, MessageDigestFileVerifier.SHA_1);
    }

    public MinecraftFileVerifier(DownloadInformation info)
    {
        FileVerifier tmp = new MessageDigestFileVerifier(MessageDigestFileVerifier.SHA_1, info.getSHA1Hash());
        verifier = new MergedFileVerifier(new SizeFileVerifier(info.getSizeInBytes()), tmp);
    }

    @Override
    public boolean isValid(File file) throws IOException
    {
        return verifier.isValid(file);
    }
}

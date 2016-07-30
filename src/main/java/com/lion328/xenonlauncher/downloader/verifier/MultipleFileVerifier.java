package com.lion328.xenonlauncher.downloader.verifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MultipleFileVerifier implements FileVerifier
{

    public static final int LOGIC_OR = 1;
    public static final int LOGIC_AND = 2;

    private final List<FileVerifier> verifiers;
    private final int logicType;

    public MultipleFileVerifier(FileVerifier first, FileVerifier second, int logicType)
    {
        this(new ArrayList<FileVerifier>(), logicType);

        verifiers.add(first);
        verifiers.add(second);
    }

    public MultipleFileVerifier(List<FileVerifier> verifiers, int logicType)
    {
        this.verifiers = verifiers;
        this.logicType = logicType;
    }

    @Override
    public boolean isValid(File file) throws IOException
    {
        for (FileVerifier verifier : verifiers)
        {
            boolean valid = verifier.isValid(file);

            if (logicType == LOGIC_OR && valid)
            {
                return true;
            }
            else if (logicType == LOGIC_AND && !valid)
            {
                return false;
            }
        }

        return false;
    }
}

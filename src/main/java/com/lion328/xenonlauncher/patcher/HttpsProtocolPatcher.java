package com.lion328.xenonlauncher.patcher;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class HttpsProtocolPatcher implements FilePatcher
{

    public static final String HTTPS_PATTERN = "^https:\\/\\/";
    public static final String HTTPS_STRING = "https://";

    private String protocol;

    public HttpsProtocolPatcher(String protocol)
    {
        this.protocol = protocol;
    }

    public String getProtocol()
    {
        return protocol;
    }

    @Override
    public byte[] patchFile(String name, byte[] original) throws Exception
    {
        if (!name.endsWith(".class"))
        {
            return original;
        }

        ClassReader reader = new ClassReader(original);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        List<FieldNode> fields = node.fields;
        for (FieldNode field : fields)
        {
            if (field.value instanceof String && ((String) field.value).startsWith(HTTPS_STRING))
            {
                field.value = ((String) field.value).replaceAll(HTTPS_PATTERN, protocol + "://");
            }
        }

        List<MethodNode> methods = node.methods;
        for (MethodNode method : methods)
        {
            InsnList insns = method.instructions;

            for (int i = 0; i < insns.size(); i++)
            {
                AbstractInsnNode insn = insns.get(i);

                if (!(insn instanceof LdcInsnNode))
                {
                    continue;
                }

                LdcInsnNode ldc = (LdcInsnNode) insn;
                if (!(ldc.cst instanceof String))
                {
                    continue;
                }

                String cst = (String) ldc.cst;
                if (!cst.startsWith(HTTPS_STRING))
                {
                    continue;
                }

                ldc.cst = cst.replaceAll(HTTPS_PATTERN, protocol + "://");
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);

        return writer.toByteArray();
    }
}

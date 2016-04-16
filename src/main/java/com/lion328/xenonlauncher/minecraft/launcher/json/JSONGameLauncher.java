package com.lion328.xenonlauncher.minecraft.launcher.json;

import com.lion328.xenonlauncher.minecraft.launcher.BasicGameLauncher;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.DependencyName;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.GameLibrary;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.GameVersion;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.MergedGameVersion;
import com.lion328.xenonlauncher.minecraft.launcher.json.exception.LauncherVersionException;
import com.lion328.xenonlauncher.minecraft.launcher.json.exception.MissingInformationException;
import com.lion328.xenonlauncher.minecraft.launcher.patcher.LibraryPatcher;
import com.lion328.xenonlauncher.util.FileUtil;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JSONGameLauncher extends BasicGameLauncher {

    private List<String> jvmArgs = new ArrayList<>();
    private List<String> gameArgs = new ArrayList<>();
    private Map<String, String> replaceArgs = new HashMap<>();
    private Map<DependencyName, LibraryPatcher> patchers = new HashMap<>();

    private GameVersion versionInfo;
    private File basepathDir;
    private File librariesDir;
    private File versionsDir;

    public JSONGameLauncher(GameVersion version, File basepathDir) throws LauncherVersionException {
        if (version.getMinimumLauncherVersion() != GameVersion.PARSER_VERSION)
            throw new LauncherVersionException("Unsupported launcher version (" + version.getMinimumLauncherVersion() + ")");

        versionInfo = version;
        this.basepathDir = basepathDir;
        this.librariesDir = new File(basepathDir, "libraries");
        this.versionsDir = new File(basepathDir, "versions");

        initialize();
    }

    private void initialize() {
        replaceArgs.put("version_name", versionInfo.getID());
        replaceArgs.put("version_type", versionInfo.getReleaseType().toString());
        replaceArgs.put("game_directory", basepathDir.getAbsolutePath());
        replaceArgs.put("user_properties", "{}");
        replaceArgs.put("user_type", "legacy");
        replaceArgs.put("auth_uuid", new UUID(0, 0).toString());
        replaceArgs.put("auth_access_token", "12345");
        replaceArgs.put("assets_index_name", versionInfo.getAssets());

        File assetsRoot = new File(basepathDir, "assets");
        if (versionInfo.getMainClass().equals("net.minecraft.launchwrapper.Launch"))
            assetsRoot = new File(assetsRoot, "virtual/legacy");

        replaceArgs.put("assets_root", assetsRoot.getAbsolutePath());
    }

    private File getDependencyFile(DependencyName name) {
        return getDependencyFile(name, "");
    }

    private File getDependencyFile(DependencyName name, String prefix) {
        String path = name.getPackageName().replace('.', '/') + "/" +
                name.getName() + "/" +
                name.getVersion() + "/" +
                name.getName() + "-" + name.getVersion() + prefix + ".jar";

        return new File(librariesDir, path);
    }

    private void extractNatives(File nativesDir) throws IOException {
        File nativesFile, parentFile;
        ZipInputStream zip;
        OutputStream out;
        ZipEntry entry;
        byte[] buffer = new byte[4096];
        int read;

        for (GameLibrary library : versionInfo.getLibraries()) {
            if (library.isNativesLibrary() && library.isAllowed()) {
                nativesFile = getDependencyFile(library.getDependencyName(),
                        "-" + library.getNatives().getNative());

                zip = new ZipInputStream(new FileInputStream(nativesFile));

                librariesLoop:
                for (; (entry = zip.getNextEntry()) != null; zip.closeEntry()) {
                    for (String exclude : library.getExtractRule().getExcludeList()) {
                        if (entry.getName().startsWith(exclude))
                            continue librariesLoop;
                    }

                    if (!entry.isDirectory()) {
                        parentFile = new File(nativesDir, entry.getName()).getParentFile();
                        if (!parentFile.exists() && !parentFile.mkdirs())
                            throw new IOException("Can't create directory");
                    }

                    out = new FileOutputStream(new File(nativesDir, entry.getName()));

                    while ((read = zip.read(buffer)) != -1)
                        out.write(buffer, 0, read);

                    out.close();
                }

                zip.close();
            }
        }
    }

    private File patchLibrary(GameLibrary original, File dir) throws Exception {
        DependencyName depName = original.getDependencyName();
        DependencyName regexDepName;

        // check first
        boolean flag = true;
        for (Map.Entry<DependencyName, LibraryPatcher> entry : patchers.entrySet()) {
            regexDepName = entry.getKey();
            if (depName.getPackageName().matches(regexDepName.getPackageName()) &&
                    depName.getName().matches(regexDepName.getName()) &&
                    depName.getVersion().matches(regexDepName.getVersion())) {
                flag = false;
                break;
            }
        }

        if (flag)
            return getDependencyFile(original.getDependencyName());

        Map<String, byte[]> classes = new HashMap<>();

        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(getDependencyFile(original.getDependencyName())));
        ZipEntry zipEntry;
        byte[] buffer = new byte[4096];
        int read;
        ByteArrayOutputStream byteTmp = new ByteArrayOutputStream();

        for (; (zipEntry = zipIn.getNextEntry()) != null; zipIn.closeEntry()) {
            if (zipEntry.isDirectory() || zipEntry.getName().endsWith(".class"))
                continue;

            byteTmp.reset();

            while ((read = zipIn.read(buffer)) != -1)
                byteTmp.write(buffer, 0, read);

            classes.put(zipEntry.getName(), byteTmp.toByteArray());
        }

        zipIn.close();

        for (Map.Entry<DependencyName, LibraryPatcher> entry : patchers.entrySet()) {
            regexDepName = entry.getKey();
            if (depName.getPackageName().matches(regexDepName.getPackageName()) &&
                    depName.getName().matches(regexDepName.getName()) &&
                    depName.getVersion().matches(regexDepName.getVersion())) {
                for (Map.Entry<String, byte[]> classEntry : classes.entrySet()) {
                    buffer = entry.getValue().patchClass(classEntry.getKey().replaceAll("\\.class$", "").replace('/', '.'), classEntry.getValue());
                    buffer = buffer.clone();
                    classEntry.setValue(buffer);
                }
            }
        }

        File outFile = new File(dir, depName.getShortName().replace(':', '-'));

        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outFile));

        for (Map.Entry<String, byte[]> classEntry : classes.entrySet()) {
            zipOut.putNextEntry(new ZipEntry(classEntry.getKey()));
            zipOut.write(classEntry.getValue());
            zipOut.closeEntry();
        }

        zipOut.close();

        return outFile;
    }

    private String buildClasspath(File patchedLibDir) throws Exception {
        StringBuilder sb = new StringBuilder();

        File versionJar = new File(versionsDir, versionInfo.getID() + "/" + versionInfo.getID() + ".jar");
        GameVersion version = versionInfo;

        while (!versionJar.isFile()) {
            if (version instanceof MergedGameVersion) {
                version = ((MergedGameVersion) version).getParent();
                versionJar = new File(versionsDir, version.getID() + "/" + version.getID() + ".jar");
            } else
                throw new MissingInformationException(versionJar.getAbsolutePath() + " is missing");
        }

        sb.append(versionJar.getAbsolutePath());

        for (GameLibrary library : versionInfo.getLibraries()) {
            if (!library.isJavaLibrary() || !library.isAllowed())
                continue;

            sb.append(File.pathSeparatorChar);
            sb.append(patchLibrary(library, patchedLibDir).getAbsolutePath());
        }

        return sb.toString();
    }

    private List<String> buildJVMArgs(File nativesDir, File patchedLibDir) throws Exception {
        List<String> list = new ArrayList<>();

        list.add("-cp");
        list.add(buildClasspath(patchedLibDir));

        list.add("-Djava.library.path=" + nativesDir.getAbsolutePath());

        list.addAll(jvmArgs);

        list.add(versionInfo.getMainClass());

        return list;
    }

    private List<String> buildGameArgs() {
        String mcArgs = versionInfo.getMinecraftArguments();

        for (Map.Entry<String, String> entry : replaceArgs.entrySet())
            mcArgs = mcArgs.replace("${" + entry.getKey() + "}", entry.getValue());

        List<String> mcArgsList = new ArrayList<>();
        mcArgsList.addAll(Arrays.asList(mcArgs.split(" ")));
        mcArgsList.addAll(gameArgs);

        return mcArgsList;
    }

    private static File findJavaExecutable() {
        return new File(System.getProperty("java.home"), "bin/java");
    }

    private List<String> buildProcessArgs(File nativesDir, File patchedLibDir) throws Exception {
        ArrayList<String> list = new ArrayList<>();
        list.add(findJavaExecutable().getAbsolutePath());
        list.addAll(buildJVMArgs(nativesDir, patchedLibDir));
        list.addAll(buildGameArgs());
        return list;
    }

    private ProcessBuilder buildProcess(File nativesDir, File patchedLibDir) throws Exception {
        System.out.println(buildProcessArgs(nativesDir, patchedLibDir));
        ProcessBuilder processBuilder = new ProcessBuilder(buildProcessArgs(nativesDir, patchedLibDir));
        processBuilder.directory(basepathDir);
        return processBuilder;
    }

    @Override
    public Process launch() throws Exception {
        File versionDir = new File(versionsDir, versionInfo.getID());
        final File nativesDir = new File(versionDir, versionInfo.getID() + "-natives-" + System.nanoTime());
        final File tmpLibraryDir = new File(versionDir, versionInfo.getID() + "-patchedlib-" + System.nanoTime());

        extractNatives(nativesDir);

        ProcessBuilder pb = buildProcess(nativesDir, tmpLibraryDir);
        final Process process = pb.start();

        final Thread removeFilesThread = new Thread("Remove natives and patched libraries") {

            @Override
            public void run() {
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                FileUtil.deleteFileRescursive(nativesDir);
                FileUtil.deleteFileRescursive(tmpLibraryDir);
            }
        };

        removeFilesThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread("Finish remove unused files thread") {

            @Override
            public void run() {
                try {
                    removeFilesThread.join(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        return process;
    }

    @Override
    public void addPatcher(DependencyName regex, LibraryPatcher patcher) {
        patchers.put(regex, patcher);
    }

    @Override
    public void replaceArgument(String key, String value) {
        replaceArgs.put(key, value);
    }

    @Override
    public void addJVMArgument(String arg) {
        jvmArgs.add(arg);
    }

    @Override
    public void addGameArgument(String arg) {
        gameArgs.add(arg);
    }
}

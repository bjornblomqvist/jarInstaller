package jarinstaller.impl;

import jarinstaller.JarInstallerException;

import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.MULTILINE;

public class Utils {

    public static Path getTargetPath(Path path) {
        File targetDir = new File(System.getProperty("user.home") + "/.jars/jars/");
        if (isWindows()) {
            targetDir = new File(System.getProperty("user.home") + "\\.jars\\jars\\");
        }
        return targetDir.toPath().resolve(path.getFileName());
    }
    
    public static boolean hasMainClassInManifest(Path jarPath) throws JarInstallerException {
        try {
            JarInputStream jarStream = new JarInputStream(new FileInputStream(jarPath.toFile()));
            Manifest mf = jarStream.getManifest();
            String mainClass = mf.getMainAttributes().getValue("Main-class");
            
            return mainClass != null;
        } catch (FileNotFoundException ex) {
            throw new JarInstallerException(ex);
        } catch (IOException ioex) {
            throw new JarInstallerException(ioex);
        }
    }
    
    
    static Pattern pattern = Pattern.compile("(.*)-(\\d+\\.\\d+.*)(javadoc|sources|).jar");
    
    public static NameAndVersion getNameAndVersion(String path) {
        String name = new File(path).getName();
        Matcher matcher = pattern.matcher(name);
        if (matcher.matches()) {
            return new NameAndVersion(matcher.group(1), matcher.group(2));
        } else if (name.contains("-")) {
            List<String> parts = new ArrayList(asList(name.split("-")));
            String version = parts.remove(parts.size() - 1);
            return new NameAndVersion(String.join("-", parts), version);
        } else {
            return new NameAndVersion(name.replace(".jar", ""), "");
        }
    }
    
    public static String getJarFileNameFor(String scriptFileName) throws IOException {
        Pattern pattern = Pattern.compile("JARINSTALLER_JAR_PATH=(.*)$", MULTILINE);

        if (isWindows()) {
            pattern = Pattern.compile("JARINSTALLER_JAR_PATH=\"(.*)\"$", MULTILINE);
        }
        
        String bashScript = new String(Files.readAllBytes(getBinDir().toPath().resolve(scriptFileName)), "UTF-8");
            
        Matcher matcher = pattern.matcher(bashScript);
        if (matcher.find()) {
            String path = matcher.group(1);
            return new File(path).toPath().getFileName().toString();
        }
        
        return null;
    }
    
    public static Path getJarPathAtBottomOfStack() throws JarInstallerException {
        try {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            return getJarPathFor(Class.forName(elements[elements.length -1].getClassName()));
        } catch (ClassNotFoundException ex) {
            throw new JarInstallerException(ex);
        }
    }
    
    public static boolean isInstalled(Path jarPath) {
        Path targetPath = Utils.getTargetPath(jarPath); 

        if (!Files.exists(jarPath) && !Files.exists(targetPath)) {
            return false;
        }

        if (jarPath.toFile().length() != targetPath.toFile().length()) {
            return false;
        }

        return true;
    }
    
    
    public static boolean install(Path jarPath) throws JarInstallerException {
        return install(jarPath, System.out);
    }
    
    public static boolean install(Path jarPath, PrintStream printStream) throws JarInstallerException {
        return install(jarPath, printStream, false);
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static boolean install(Path jarPath, PrintStream printStream, boolean installingSelf) throws JarInstallerException {
        try {
            if (Files.isDirectory(jarPath) || !Files.exists(jarPath)) {
                throw new JarInstallerException("Install should only be called from inside a JAR file, path: " + jarPath);
            }
            
            if (! Utils.hasMainClassInManifest(jarPath)) {
                throw new JarInstallerException("Jar file Manifest does not have Main-class, " + jarPath);
            }

            File targetDir = getJarsDir();
            File targetBinDir = getBinDir();

            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            if (!targetBinDir.exists()) {
                targetBinDir.mkdirs();
            }

            Path targetPath = targetDir.toPath().resolve(jarPath.getFileName());

            Files.copy(jarPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            if (installingSelf) {
                printStream.println("Copied self to ~/.jars/jars/" + jarPath.getFileName());
            } else {
                printStream.println("Copied " + jarPath + " to ~/.jars/jars/" + jarPath.getFileName());
            }
            
            if (!System.getenv("PATH").contains("/.jars/bin")) {
                Path profilePath = new File(System.getProperty("user.home") + "/.profile").toPath();
                boolean hasAlreadyBeenAdded = false;
                if (Files.exists(profilePath)) {
                    String profile = new String(Files.readAllBytes(profilePath));
                    hasAlreadyBeenAdded = profile.contains("/.jars/bin");
                }
                if (!hasAlreadyBeenAdded) {
                    printStream.println("Adding ~/.jars/bin to $PATH. Made changes to ~/.profile");
                    Files.write(
                        profilePath,
                        "\nPATH=$PATH:$HOME/.jars/bin # Add jarinstaller bin to PATH\n".getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                    );
                }
            }
            
            NameAndVersion nameAndVersion = getNameAndVersion(jarPath.toString());
            
            String targetBashScript = targetBinDir.toPath().resolve(nameAndVersion.name).toString();

            if (isWindows()) {
                writeCmdScript(targetBashScript + ".cmd", targetPath, printStream);
            } else {
                writeBashScript(targetBashScript, targetPath, printStream);
            }
        } catch (IOException ioex) {
            throw new JarInstallerException(ioex);
        }
        
        return true;
    }

    private static void writeCmdScript(String targetCmdScript, Path targetPath, PrintStream printStream) throws IOException {
        try(FileWriter bashScriptOutputstream = new FileWriter(targetCmdScript);) {
            bashScriptOutputstream.write(
                "\r\n" +
                "@echo off\r\n" +
                "\r\n" +
                "set JARINSTALLER_PATH=\" " + getJarInstallerDir() + "\"\r\n" +
                "set JARINSTALLER_JAR_PATH=\"" + targetPath + "\"\r\n" +
                "set JARINSTALLER_SCRIPT_PATH=\"" + targetCmdScript + "\"\r\n" +
                "\r\n" +
                "java -jar %JARINSTALLER_JAR_PATH% %*\r\n" +
                "exit\r\n"
            );
        }

        printStream.println("Created cmd script ~/.jars/bin/" + Paths.get(targetCmdScript).getFileName());
    }

    private static void writeBashScript(String targetBashScript, Path targetPath, PrintStream printStream) throws IOException {
        try(FileWriter bashScriptOutputstream = new FileWriter(targetBashScript);) {
            bashScriptOutputstream.write("#!/bin/bash\n" +
                "\n" +
                "export JARINSTALLER_PATH=~/.jars/\n" +
                "export JARINSTALLER_JAR_PATH=" + targetPath + "\n" +
                "export JARINSTALLER_SCRIPT_PATH=" + targetBashScript + "\n" +
                "\n" +
                "java -jar $JARINSTALLER_JAR_PATH \"$@\"\n");
        }

        Set<PosixFilePermission> perms = Files.getPosixFilePermissions(Paths.get(targetBashScript));
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(Paths.get(targetBashScript), perms);

        printStream.println("Created bash script ~/.jars/bin/" + Paths.get(targetBashScript).getFileName());
    }
    
    
    public static boolean unInstall(Path jarPath) throws JarInstallerException {
        return unInstall(jarPath, System.out);
    }

    private static void deleteJarOnWindows(Path jarPath) throws IOException {
        File scriptFile = File.createTempFile("self-destruct", ".bat" );

        try (PrintWriter printWriter = new PrintWriter(new FileWriter(scriptFile))) {
            printWriter.println("ping -n 1 127.0.0.1 > nul");
            printWriter.println("DEL /F \"" + jarPath + "\"");
            printWriter.println("start /b \"\" cmd /c del \"%~f0\"&exit /b");
        }

        runAndForgetCmd(scriptFile.getAbsolutePath());
    }

    private static void runAndForgetCmd(String cmdPath) throws IOException {
        List<String> commands = new ArrayList();
        commands.addAll(asList("cmd", "/c", "start", "/b"));
        commands.add(cmdPath.replace("/", "\\"));

        File logFile = File.createTempFile("logfile", ".log" );

        new ProcessBuilder(commands.toArray(new String[commands.size()]))
            .redirectError(logFile)
            .redirectOutput(logFile)
            .start();
    }
    
    public static boolean unInstall(Path jarPath, PrintStream printStream) throws JarInstallerException {
        try {
            String scriptName = "";
            String jarName = "";

            if (!jarPath.toString().endsWith(".jar")) {
                scriptName = jarPath.getFileName().toString();

                if (isWindows()) {
                    scriptName += ".cmd";
                }

                if (!Files.exists(getBinDir().toPath().resolve(scriptName))) {
                    System.out.println("There is no " + scriptName + " in ~/.jars/bin/");
                    return false;
                }
                jarName = getJarFileNameFor(scriptName);
            } else {
                jarName = jarPath.getFileName().toString();
                NameAndVersion nameAndVersion = getNameAndVersion(jarPath.toString());
                scriptName = nameAndVersion.name;

                if (isWindows()) {
                    scriptName += ".cmd";
                }
            }

            Path targetPath = getJarsDir().toPath().resolve(new String(jarName));
            Path targetBashScript = getBinDir().toPath().resolve(scriptName);

            if (!Files.exists(targetPath) && !Files.exists(targetBashScript)) {
                printStream.println("There is no " + jarName + " in ~/.jars/jars/ and no " + scriptName + " in ~/.jars/bin/");
                return false;
            }
            
            if (Files.exists(targetPath)) {
                printStream.println("Removing ~/.jars/jars/" + jarName);
                if (isWindows()) {
                    deleteJarOnWindows(targetPath);
                } else {
                    Files.delete(targetPath);
                }
            }
        
            if (Files.exists(targetBashScript)) {
                printStream.println("Removing ~/.jars/bin/" + scriptName);
                if (isWindows()) {
                    deleteJarOnWindows(targetBashScript);
                } else {
                    Files.delete(targetBashScript);
                }
            }
            
        } catch (IOException ioex) {
            throw new JarInstallerException(ioex);
        }
        
        return true;
    }
    
    public static boolean isInJarsDirectory(Path jarPath) {
        Path targetPath =  Utils.getTargetPath(jarPath); 

        if (!Files.exists(jarPath) && !Files.exists(targetPath)) {
            return false;
        }

        return jarPath.equals(targetPath);
    }
    
    public static Path getJarPathFor(Class mainClass) throws JarInstallerException {
        try {
            return new File(mainClass.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).toPath();
        } catch (URISyntaxException e) {
            throw new JarInstallerException("Failed find to jar", e);
        }
    }

    public static File getJarInstallerDir() {
        return new File(System.getProperty("user.home") + "/.jars/");
    }
    
    public static File getJarsDir() {
        return new File(System.getProperty("user.home") + "/.jars/jars/");
    }
    
    public static File getBinDir() {
        return new File(System.getProperty("user.home") + "/.jars/bin/");
    }
    
    public static class NameAndVersion {
        
        public String name;
        public String version;
        
        public NameAndVersion(String name, String version) {
            this.name = name;
            this.version = version;
        }
    }
}

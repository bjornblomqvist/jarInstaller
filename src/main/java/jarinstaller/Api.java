package jarinstaller;

import jarinstaller.impl.Utils;
import jarinstaller.impl.Utils.NameAndVersion;
import static jarinstaller.impl.Utils.getBinDir;
import static jarinstaller.impl.Utils.getJarFileNameFor;
import static jarinstaller.impl.Utils.getJarsDir;
import static jarinstaller.impl.Utils.getNameAndVersion;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class Api {
    
    public static boolean unInstall(Path jarPath) throws JarInstallerException {
        return unInstall(jarPath, new PrintStream(new ByteArrayOutputStream()));
    }
    
    public static boolean unInstall(Path jarPath, PrintStream printStream) throws JarInstallerException {
        try {
            String scriptName = "";
            String jarName = "";

            if (!jarPath.toString().endsWith(".jar")) {
                scriptName = jarPath.getFileName().toString();
                if (!Files.exists(getBinDir().toPath().resolve(scriptName))) {
                    System.out.println("There is no " + scriptName + " in ~/.jars/bin/");
                    return false;
                }
                jarName = getJarFileNameFor(scriptName);
            } else {
                jarName = jarPath.getFileName().toString();
                NameAndVersion nameAndVersion = getNameAndVersion(jarPath.toString());
                scriptName = nameAndVersion.name;
            }

            Path targetPath = getJarsDir().toPath().resolve(new String(jarName));
            Path targetBashScript = getBinDir().toPath().resolve(scriptName);

            if (!Files.exists(targetPath) && !Files.exists(targetBashScript)) {
                printStream.println("There is no " + jarName + " in ~/.jars/jars/ and no " + scriptName + " in ~/.jars/bin/");
                return false;
            }
            
            if (Files.exists(targetPath)) {
                printStream.println("Removing ~/.jars/jars/" + jarName);
                Files.delete(targetPath);
            }
        
            if (Files.exists(targetBashScript)) {
                printStream.println("Removing ~/.jars/bin/" + scriptName);
                Files.delete(targetBashScript);
            }
            
        } catch (IOException ioex) {
            throw new JarInstallerException(ioex);
        }
        
        return true;
    }
    
    public static boolean install(Path jarPath) throws JarInstallerException {
        return install(jarPath, System.out);
    }
    
    public static boolean install(Path jarPath, PrintStream printStream) throws JarInstallerException {
        return install(jarPath, printStream, false);
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
        } catch (IOException ioex) {
            throw new JarInstallerException(ioex);
        }
        
        return true;
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
    
    public static boolean isInJarsDirectory(Path jarPath) {
        Path targetPath =  Utils.getTargetPath(jarPath); 

        if (!Files.exists(jarPath) && !Files.exists(targetPath)) {
            return false;
        }

        return jarPath.equals(targetPath);
    }
    
    public static Path getJarPathFor(Class mainClass) throws JarInstallerException {
        try {
            if (mainClass == null) {
                throw new JarInstallerException("Failed find to jar");
            }
            
            return new File(mainClass.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).toPath();
        } catch (URISyntaxException e) {
            throw new JarInstallerException("Failed find to jar", e);
        }
    }
}

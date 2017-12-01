package jarinstaller.impl;

import jarinstaller.JarInstallerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.MULTILINE;

public class Utils {

    public static Path getTargetPath(Path path) {
        File targetDir = new File("~/.jars/jars/".replaceFirst("^~", System.getProperty("user.home")));
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
        
        String bashScript = new String(Files.readAllBytes(getBinDir().toPath().resolve(scriptFileName)), "UTF-8");
            
        Matcher matcher = pattern.matcher(bashScript);
        if (matcher.find()) {
            String path = matcher.group(1);
            return new File(path).toPath().getFileName().toString();
        }
        
        return null;
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

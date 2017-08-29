package jarinstaller.impl;

import jarinstaller.JarInstallerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

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
    
}

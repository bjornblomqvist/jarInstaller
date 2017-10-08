package jarinstaller.cmdline.classpath;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DependencyLoader {

    public static void init() throws IOException, URISyntaxException {
        URLClassLoader toTweek = (URLClassLoader) DependencyLoader.class.getClassLoader();
        Handler.setup(toTweek);
        List<String> paths = getResourceListing(toTweek, "dependencies/");
        for (String path : paths) {
            if (path.endsWith(".jar")) {
                addURL(toTweek, "classpath:dependencies/" + path);
            }
        }
    }
    
    private static void addURL(URLClassLoader urlClassLoader, String url) {
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{ URL.class });
            method.setAccessible(true);
            method.invoke(urlClassLoader, new Object[]{new URL(url)});
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Error, could not add URL to system classloader", t);
        }
    }
    
    private static List<String> getResourceListing(URLClassLoader urlClassLoader, String path) throws URISyntaxException, IOException {
      URL dirURL = urlClassLoader.getResource(path);
      if (dirURL != null && dirURL.getProtocol().equals("file")) {
        /* A file path: easy enough */
        return asList(new File(dirURL.toURI()).list());
      } 

      if (dirURL == null) {
        return Collections.emptyList();
      }
      
      if (dirURL.getProtocol().equals("jar")) {
        /* A JAR path */
        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
        Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
        Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
        while(entries.hasMoreElements()) {
          String name = entries.nextElement().getName();
          if (name.startsWith(path)) { //filter according to the path
            String entry = name.substring(path.length());
            int checkSubdir = entry.indexOf("/");
            if (checkSubdir >= 0) {
              // if it is a subdirectory, we just return the directory name
              entry = entry.substring(0, checkSubdir);
            }
            result.add(entry);
          }
        }
        
        return asList(result.toArray(new String[result.size()]));
      } 
        
      throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
    }
}

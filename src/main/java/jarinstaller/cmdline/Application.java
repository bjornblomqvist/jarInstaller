package jarinstaller.cmdline;

import static jarinstaller.Api.*;
import jarinstaller.JarInstallerException;
import jarinstaller.cmdline.classpath.DependencyLoader;
import jarinstaller.impl.Utils.NameAndVersion;
import static jarinstaller.impl.Utils.getBinDir;
import static jarinstaller.impl.Utils.getJarFileNameFor;
import static jarinstaller.impl.Utils.getJarsDir;
import static jarinstaller.impl.Utils.getNameAndVersion;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.MULTILINE;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Application {
    
    public static void main(String...args) throws JarInstallerException, IOException, URISyntaxException {
        
        DependencyLoader.init();
        
        Inner.main(args);
    }

    private static class Inner {
        
        public static void main(String...args) throws JarInstallerException, IOException {
            if (System.getenv("HOME") != null) {
                System.setProperty("user.home", System.getenv("HOME"));
            }
            
            OptionParser parser = new OptionParser();
            parser.accepts("help");
            parser.accepts("version");
            parser.accepts("h");
            parser.accepts("install-self");

            OptionSet optionSet;

            try {
                optionSet = parser.parse(args);
            } catch (joptsimple.OptionException e) {
                System.err.println("\n" + e.getMessage()+"\n");
                System.err.println("Use --help to find out how to use autotest.\n");
                return;
            }

            if (optionSet.has("h") || optionSet.has("help")) {
                printHelp();

                return;
            }
            
            if (optionSet.has("version")) {
                printVersion();

                return;
            }
            
            if (optionSet.has("install-self")) {
                installSelf();
                return;
            }

            List<String> nonOptions = (List<String>) new ArrayList(optionSet.nonOptionArguments());
            nonOptions.removeIf((o) -> o == null || o.trim().isEmpty());
            if (nonOptions.isEmpty()) {            
                printHelp();
                return;
            }

            if (nonOptions.get(0).equals("install")) {
                if (nonOptions.size() == 1) {
                    System.err.println(
                        "Install action needs a jar file path.\n" +
                        "\n" +
                        "Like this: jarinstaller install path/to/your.jar\n"
                    );

                    return;
                }

                install(new File(nonOptions.get(1)).toPath(), System.out);
            } else if (nonOptions.get(0).equals("uninstall")) {
                if (nonOptions.size() == 1) {
                    System.err.println(
                        "Uninstall action needs a jar file path.\n" +
                        "\n" +
                        "Like this: jarinstaller uninstall path/to/your.jar\n"
                    );

                    return;
                }

                unInstall(new File(nonOptions.get(1)).toPath(), System.out);
            } else if (nonOptions.get(0).equals("list")) {
                listJars();
            } else {
                System.out.println("\nERROR! unknown param, \"" + nonOptions.get(0) + "\"");
                printHelp();
            }
        }
    }
    
    private static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);  
    }
    
    private static void listJars() throws IOException {
        System.out.println("\n\tInstalled jars\n");
        
        Pattern pattern = Pattern.compile("JARINSTALLER_JAR_PATH=(.*)$", MULTILINE);
        
        String[] fileNames = getBinDir().list();
        int maxLength = 0;
        for (String fileName : fileNames) {
            maxLength = Math.max(maxLength, fileName.length());
        }
        
        for (String fileName : fileNames) {
            
            String jarFileName = getJarFileNameFor(fileName);
            
            if (jarFileName != null) {
                NameAndVersion nameAndVersion = getNameAndVersion(jarFileName);
                
                String missingJar = new File(getJarsDir().toPath().resolve(jarFileName).toString()).exists() ? "" : " (missing jar)";
                String version = nameAndVersion.version.trim().length() == 0 ? "" : " (" + nameAndVersion.version + ")";
                
                System.out.println(padRight(fileName, maxLength) + " -> " + jarFileName + missingJar);
            }
        }
        
        System.out.println("");
    }
    
    private static void installSelf() throws JarInstallerException {
        install(getJarPathFor(Application.class), System.out, true);
    }
    
    public static Properties readManifest(Class<?> clz) {
        String resource = "/" + clz.getName().replace(".", "/") + ".class";
        String fullPath = clz.getResource(resource).toString();
        String archivePath = fullPath.substring(0, fullPath.length() - resource.length());

        try (InputStream input = new URL(archivePath + "/META-INF/MANIFEST.MF").openStream()) {
            Properties prop = new Properties();
            prop.load(input);
            return prop;
            
        } catch (Exception e) {
            throw new RuntimeException("Loading MANIFEST for class " + clz + " failed!", e);
        }
    }
    
    private static void printVersion() {
        Properties prop = readManifest(Application.class);
        System.out.println("jarinstaller " + prop.getProperty("App-version"));
    }
    
    private static void printHelp() {
        System.out.println(
            "\n" +
            "usage: jarinstaller [--help] [install|uninstall] [jarfile]\n" +
            "\n" +
            "jarInstaller is used to install runnable jars and map them\n" +
            "to a command in the path.\n" +
            "\n"+
            "   install         installes a jar file\n" +
            "   uninstall       uninstalles a jar file\n" +
            "   list            list installed jars\n" +
            "\n" +
            "   -h, --help      show help\n" +
            "   --install-self  installes jarinstaller\n" +
            "   --version       prints current version\n" + 
            "\n"
        );
    }
}

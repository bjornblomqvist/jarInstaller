package jarinstaller.cmdline;

import static jarinstaller.Api.*;
import jarinstaller.JarInstallerException;
import jarinstaller.cmdline.classpath.DependencyLoader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Application {
    
    public static void main(String...args) throws JarInstallerException, IOException, URISyntaxException {
        
        DependencyLoader.init();
        
        Inner.main(args);
    }
    
    private static class Inner {
        
        public static void main(String...args) throws JarInstallerException, IOException {

            OptionParser parser = new OptionParser();
            parser.accepts("help");
            parser.accepts("h");

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
                        "Like this: jarInstaller install path/to/your.jar\n"
                    );

                    return;
                }

                install(new File(nonOptions.get(1)).toPath(), System.out);
            } else if (nonOptions.get(0).equals("uninstall")) {
                if (nonOptions.size() == 1) {
                    System.err.println(
                        "Uninstall action needs a jar file path.\n" +
                        "\n" +
                        "Like this: jarInstaller uninstall path/to/your.jar\n"
                    );

                    return;
                }

                unInstall(new File(nonOptions.get(1)).toPath(), System.out);
            }   
        }
    }

    private static void printHelp() {
        System.out.println(
                "\n" +
                "usage: jarInstaller [--help] [install|uninstall] [jarfile]\n" +
                "\n" +
                "jarInstaller is used to install runnable jars and map them\n" +
                "to a command in the path.\n" +
                "\n"+
                "   install     Installes a jar file\n" +
                "   uninstall   Uninstalles a jar file\n" +
                "\n" +
                "   -h, --help  show help\n" +
                "\n"
            );
    }
}

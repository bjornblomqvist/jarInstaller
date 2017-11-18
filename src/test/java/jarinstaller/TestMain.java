package jarinstaller;

import static jarinstaller.Api.getJarPathFor;

/**
 * Used to test parts of the API that is only relevant from within a jar.
 */
public class TestMain {
    public static void main(String[] args) throws JarInstallerException {
        System.setProperty("user.home", System.getenv("HOME"));
        
        if (args[0].equalsIgnoreCase("install")) {
            System.out.println(Api.install(getJarPathFor(TestMain.class)));
        }
        
        if (args[0].equalsIgnoreCase("unInstall")) {
            System.out.println(Api.unInstall(getJarPathFor(TestMain.class)));
        }
        
        if (args[0].equalsIgnoreCase("isInstalled")) {
            System.out.println(Api.isInstalled(getJarPathFor(TestMain.class)));
        }
        
        if (args[0].equalsIgnoreCase("isInJarsDirectory")) {
            System.out.println(Api.isInJarsDirectory(getJarPathFor(TestMain.class)));
        }
    }
}

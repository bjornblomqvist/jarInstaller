package jarinstaller;

import static jarinstaller.JarInstaller.getJarPathFor;

public class TestMain {
    public static void main(String[] args) throws JarInstallerException {
        System.setProperty("user.home", System.getenv("HOME"));
        
        if (args[0].equalsIgnoreCase("install")) {
            System.out.println(JarInstaller.install(getJarPathFor(TestMain.class)));
        }
        
        if (args[0].equalsIgnoreCase("unInstall")) {
            System.out.println(JarInstaller.unInstall(getJarPathFor(TestMain.class)));
        }
        
        if (args[0].equalsIgnoreCase("isInstalled")) {
            System.out.println(JarInstaller.isInstalled(getJarPathFor(TestMain.class)));
        }
        
        if (args[0].equalsIgnoreCase("isInJarsDirectory")) {
            System.out.println(JarInstaller.isInJarsDirectory(getJarPathFor(TestMain.class)));
        }
    }
}

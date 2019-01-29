package jarinstaller;

import static jarinstaller.impl.Utils.getJarPathAtBottomOfStack;


public class Api {
    
    /**
     * Removes the current jar and script from ~/.jars/
     *
     * @return true on success
     * @throws JarInstallerException if there is any exception
     */
    public static boolean unInstall() throws JarInstallerException {
        return jarinstaller.impl.Utils.unInstall(getJarPathAtBottomOfStack());
    }
    
    /**
     * Copies the current jar to ~/.jars/jars and creates a bash script in ~/.jars/bin
     *
     * @return true on success
     * @throws JarInstallerException if there is any exception
     */
    public static boolean install() throws JarInstallerException {
        return jarinstaller.impl.Utils.install(getJarPathAtBottomOfStack());
    }
    
    /**
     * Returns true if a jar with the same name and size can be found in ~/.jars/jars
     *
     * @return true if a jar with the same name exists
     * @throws JarInstallerException if there is any exception
     */
    public static boolean isInstalled() throws JarInstallerException {
        return jarinstaller.impl.Utils.isInstalled(getJarPathAtBottomOfStack());
    }
    
    /**
     * Returns true if it is run from a jar in ~/.jars/jars
     *
     * @return true if it is run from a jar in ~/.jars/jars
     * @throws JarInstallerException if there is any exception
     */
    public static boolean isRunFromJarsDir() throws JarInstallerException {
        return jarinstaller.impl.Utils.isInJarsDirectory(getJarPathAtBottomOfStack());
    }
}

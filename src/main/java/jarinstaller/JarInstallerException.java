package jarinstaller;

public class JarInstallerException extends Exception {

    
    public JarInstallerException(String message) {
        super(message);
    }
    
    public JarInstallerException(Exception cause) {
        super(cause);
    }
    
    public JarInstallerException(String message, Exception cause) {
        super(message, cause);
    }
}

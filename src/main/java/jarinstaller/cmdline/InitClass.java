package jarinstaller.cmdline;

import java.io.IOException;
import java.net.URISyntaxException;

import jarinstaller.cmdline.classpath.DependencyLoader;

public class InitClass {
    public static void main(String...args) throws IOException, URISyntaxException {
        DependencyLoader.init("jarinstaller.cmdline.Application", args);
    }
}

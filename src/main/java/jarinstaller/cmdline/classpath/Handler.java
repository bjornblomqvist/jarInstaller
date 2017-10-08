package jarinstaller.cmdline.classpath;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.UnknownServiceException;

public class Handler extends URLStreamHandler {

    static ClassLoader classLoader;

    public static void setup(ClassLoader classLoader) {
        Handler.classLoader = classLoader;
        String pkgs = "jarinstaller.cmdline";

        if(System.getProperties().contains("java.protocol.handler.pkgs")) {
            pkgs = System.getProperty("java.protocol.handler.pkgs") + "|" + pkgs;
        }

        System.getProperties().put("java.protocol.handler.pkgs", pkgs);
    }

    protected URLConnection openConnection(URL u) throws IOException {
        return new Connection(u);
    }

    private static class Connection extends URLConnection {

        Connection(URL url) {
            super(url);
        }

        public void connect() {
        }

        public InputStream getInputStream() throws IOException {
            String resourcePath = url.toString().replaceAll("^classpath:(/|)", "");
            InputStream inputStream = Handler.classLoader.getResourceAsStream(resourcePath);

            if(inputStream == null) {
                throw new IOException("Resource not found: " + url.getFile());
            }

            return inputStream;
        }

        public OutputStream getOutputStream() throws IOException {
            throw new UnknownServiceException("Output is not supported");
        }
    }
}


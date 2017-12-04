[![Build Status](https://travis-ci.org/bjornblomqvist/jarInstaller.svg?branch=master)](https://travis-ci.org/bjornblomqvist/jarInstaller)

# jarinstaller

Jar installer is a simple library and command line tool for installing runnable
jars. It adds the `~/.jars/jars` and puts a bash script in `~/.jars/bin`.

Once a jar has been installed it will be as easy to run as any other program.

## Using the library
	
You can create a self installing jar as simple as seen below.

    import static jarinstaller.Api.*;

    public class Application {
        public static void main(String...args) throws JarInstallerException {
            if (args.length == 0) {
                if (isInstalled()) {
                    System.out.println("Call with --uninstall to uninstall");
                } else {
                    System.out.println("Call with --install to install");
                }

                return;
            }

            if (args[0].equals("--install")) {
                install();
            }

            if (args[0].equals("--uninstall")) {
                unInstall();
            }
        }
    }

## Using the command line tool

    $ jarinstaller install filetransfer-1.0.jar

    Copied filetransfer-1.0.jar to ~/.jars/jars/filetransfer-1.0.jar
    Created bash script ~/.jars/bin/filetransfer
    
## Install the command line tool

    curl -OL https://github.com/bjornblomqvist/jarInstaller/releases/download/0.1.0/jarinstaller-0.1.0.jar
    java -jar jarinstaller-0.1.0.jar --install-self
    export PATH=$PATH:~/.jars/bin

    jarinstaller --help
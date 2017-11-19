[![Build Status](https://travis-ci.org/bjornblomqvist/jarInstaller.svg?branch=master)](https://travis-ci.org/bjornblomqvist/jarInstaller)

# Jar Installer

Jar installer is a simple library and command line tool for installing runnable
jars. It adds the `~/.jars/jars` and puts a bash script in `~/.jars/bin`.

Once a jar has been installed it will be as easy to run as any other program.

## Using the library
	
You can create a self installing jar by including the library and using its API
from inside the main method to install it.

    import static jarinstaller.Api.*;
    
    public class Application {
        public static void main(String...args) {
            if (args[0].equals("--install")) {
                install(getJarPathFor(Application.class));
            }
        }
    }

## Installing using the command line tool

    jarinstaller install a-runnable.jar
    
## Installing the command line tool

    curl -O https://github.com/bjornblomqvist/jarInstaller...
    java -jar jarinstall.jar --install-self --add-to-path
    
    jarinstaller --help
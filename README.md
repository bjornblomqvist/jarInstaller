[![Build Status](https://travis-ci.org/bjornblomqvist/jarInstaller.svg?branch=master)](https://travis-ci.org/bjornblomqvist/jarInstaller)

# jarinstaller

Jar installer is a library and command line tool for installing runnable jars.
For each jar installed it puts the jar in  `~/.jars/jars` and creates a bash
script in `~/.jars/bin` that runs the jar file.

Once a jar has been installed it is as easy to run as any other command line
program.

**The goal of this project is to make java a good choice for creating command
line tools.**

## Using the library

The library is used to create self installing jars. See the example below for
how to make your runnable jar self installing.

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

## Gradle and maven dependency

```xml
<dependency>
    <groupId>se.bjornblomqvist</groupId>
    <artifactId>jarinstaller</artifactId>
    <version>0.2.0</version>
</dependency>
```

```groovy
compile 'se.bjornblomqvist:jarinstaller:0.2.0'
```

## Command line tool

The command line tool is used for installing runnable jars. It can be used to install
any self contained runnable jar.

    $ jarinstaller install filetransfer-1.0.jar

    Copied filetransfer-1.0.jar to ~/.jars/jars/filetransfer-1.0.jar
    Created bash script ~/.jars/bin/filetransfer
 
 Run without any arguements to get the help.

    $ jarinstaller

    usage: jarinstaller [--help] [install|uninstall] [jarfile]

    jarinstaller is used to install runnable jars and map them
    to a command in the path.

       install         installes a jar file
       uninstall       uninstalles a jar file
       list            list installed jars

       -h, --help      show help
       --install-self  installes jarinstaller
       --version       prints current version

## Getting the command line tool

Use the install script or follow the manual instructions.

    curl -sSL https://git.io/vbSHD | bash -s stable && export PATH=$PATH:~/.jars/bin

Follow these steps to install jarinstaller manually.

    curl -OL http://repo1.maven.org/maven2/se/bjornblomqvist/jarinstaller/0.2.0/jarinstaller-0.2.0.jar
    java -jar jarinstaller-0.2.0.jar --install-self
    export PATH=$PATH:~/.jars/bin

## Contribute

Help is welcomed! Please file an issue or pull request.


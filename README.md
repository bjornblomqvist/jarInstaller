# Jar Installer

Jar installer is a simple library and command line tool for installing runnable
jars in your home directory `~/.jars` and adding `~/.jars/bin` to your PATH.

Once a jar has been installed it will be as easy to run as any other program.

    curl -O http://www.catacombae.org/typhertransfer/filetransfer.jar
    jarInstaller install filetransfer.jar
    filetransfer

## Use the lib
	
Just add a single line of code. Where Application.class is in the jar file that
you wish to install.

    JarInstaller.install(getJarPathFor(Application.class));

## Install command line tool

    git clone git@github.com:bjornblomqvist/jarInstaller.git
    cd jarInstaller
    mvn package

    java -jar target/jarinstaller-0.1.0-SNAPSHOT.jar --install --add-to-path
    

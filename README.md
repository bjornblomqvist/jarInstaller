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

    curl -O central.maven.org/maven2/org/jarinstall/jarinstaller/1.0.0/jarInstaller-1.0.0.jar
    
    java -jar jarInstaller-1.0.0.jar --install --add-to-path

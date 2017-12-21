curl -OL http://repo1.maven.org/maven2/se/bjornblomqvist/jarinstaller/0.2.0/jarinstaller-0.2.0.jar
java -jar jarinstaller-0.2.0.jar --install-self
export PATH=$PATH:~/.jars/bin

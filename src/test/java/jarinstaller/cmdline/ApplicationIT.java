package jarinstaller.cmdline;

import com.greghaskins.spectrum.Spectrum;
import com.greghaskins.spectrum.Variable;
import static com.greghaskins.spectrum.dsl.specification.Specification.*;
import static jarinstaller.ApiTest.buildTestJars;
import static jarinstaller.ApiTest.runJar;
import static jarinstaller.ApiTest.tryToDelete;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.runner.RunWith;

@RunWith(Spectrum.class)
public class ApplicationIT {
    
    {
        String cwd = Paths.get(".").toAbsolutePath().normalize().toString();
        String ORIGINAL_HOME = System.getProperty("user.home");
        String DUMMY_HOME = cwd + "/target/dummy_home/";
        
        describe("Application", () -> {
            
            beforeAll(() -> {
                buildTestJars();
                new File(DUMMY_HOME).mkdirs();
                System.setProperty("user.home", DUMMY_HOME);
            });
        
            afterAll(() -> {
                System.setProperty("user.home", ORIGINAL_HOME);
            });
            
            beforeEach(() -> {
                tryToDelete(new File(DUMMY_HOME + ".jars/jars/test.jar"));
                tryToDelete(new File(DUMMY_HOME + ".jars/jars/"));
                tryToDelete(new File(DUMMY_HOME + ".jars/bin/test"));
                tryToDelete(new File(DUMMY_HOME + ".jars/bin/"));
                tryToDelete(new File(DUMMY_HOME + ".jars/"));
                
                Files.write(new File(DUMMY_HOME + ".profile").toPath(), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            });
            
            context("no params", () -> {
                Variable<String> stdout = new Variable();
                
                beforeEach(() -> {
                    stdout.set(runJar("./target/jarinstaller-0.1.0-SNAPSHOT.jar", ""));
                });
                
                it("should show the help", () -> {
                    assertThat(stdout.get(), containsString("usage: jarinstaller"));
                });
            });
            
            describe("--help", () -> {
                
                Variable<String> stdout = new Variable();
                
                beforeEach(() -> {
                    stdout.set(runJar("./target/jarinstaller-0.1.0-SNAPSHOT.jar", "--help"));
                });

                it("should show the help", () -> {
                    assertThat(stdout.get(), containsString("usage: jarinstaller"));
                });
            });
            
            describe("install", () -> {
                Variable<String> stdout = new Variable();
                
                beforeEach(() -> {
                    stdout.set(runJar("./target/jarinstaller-0.1.0-SNAPSHOT.jar", "install"));
                });
                
                it("should print that it needs a jar file path", () -> {
                    assertThat(stdout.get(), containsString("Install action needs a jar file path."));
                });
                
                it("should print an example", () -> {
                    assertThat(stdout.get(), containsString("jarinstaller install path/to/your.jar"));
                });
            });
            
            describe("install target/test.jar", () -> {
                
                Variable<String> stdout = new Variable();
                
                beforeEach(() -> {
                    stdout.set(runJar("./target/jarinstaller-0.1.0-SNAPSHOT.jar", "install", "target/test.jar"));
                });
                
                it("should print that it copied the jar", () -> {
                    assertThat(stdout.get(), containsString("Copied target/test.jar to ~/.jars/jars/test.jar"));
                });
               
                it("should print that it created bash script", () -> {
                    assertThat(stdout.get(), containsString("Created bash script ~/.jars/bin/test"));
                });
                
                it("should add the jar to the jars directory", () -> {
                   assertThat(new File(DUMMY_HOME+".jars/jars/test.jar").exists(), is(true));
                });
               
                it("should add script file to bin directory", () -> {
                   assertThat(new File(DUMMY_HOME+".jars/bin/test").exists(), is(true));
                }); 
                
                context("~/.jars/bin is not in PATH", () -> {
                    
                    beforeEach(() -> {
                        Files.write(new File(DUMMY_HOME + ".profile").toPath(), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                        HashMap<String, String> env = new HashMap();
                        env.put("PATH", "");
                        stdout.set(runJar("./target/jarinstaller-0.1.0-SNAPSHOT.jar", env, "install", "target/test.jar"));
                    });
                    
                    it("should add sippet that adds the path to ~/.profile", () -> {
                        String profileContent = new String(Files.readAllBytes(new File(DUMMY_HOME+".profile").toPath()));
                        assertThat(profileContent, containsString("PATH=$PATH:$HOME/.jars/bin # Add jarinstaller bin to PATH"));
                    });
                });
                
                context("~/.jars/bin is in PATH", () -> {
                    beforeEach(() -> {
                        Files.write(new File(DUMMY_HOME + ".profile").toPath(), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                        HashMap<String, String> env = new HashMap();
                        env.put("PATH", "/.jars/bin");
                        stdout.set(runJar("./target/jarinstaller-0.1.0-SNAPSHOT.jar", env, "install", "target/test.jar"));
                    });
                    
                    it("should not add a sippet to ~/.profile", () -> {
                        String profileContent = new String(Files.readAllBytes(new File(DUMMY_HOME+".profile").toPath()));
                        assertThat(profileContent, not(containsString("PATH=$PATH:$HOME/.jars/bin # Add jarinstaller bin to PATH")));
                    });
                });
            });
            
            describe("--install-self", () -> {
                
                Variable<String> stdout = new Variable();
                
                beforeEach(() -> {
                    stdout.set(runJar("./target/jarinstaller-0.1.0-SNAPSHOT.jar", "--install-self"));
                });
                
                it("should print that it copied the jar", () -> {
                    assertThat(stdout.get(), containsString("Copied self to ~/.jars/jars/jarinstaller-"));
                });
               
                it("should print that it created bash script", () -> {
                    assertThat(stdout.get(), containsString("Created bash script ~/.jars/bin/jarinstaller"));
                });
                
                it("should add script file to bin directory", () -> {
                   assertThat(new File(DUMMY_HOME+".jars/bin/jarinstaller-0").exists(), is(true));
                }); 
                
                it("should add the jar to the jars directory", () -> {
                   assertThat(new File(DUMMY_HOME+".jars/jars/jarinstaller-0.1.0-SNAPSHOT.jar").exists(), is(true));
                });
            });
            
            describe("uninstall target/test.jar", () -> {
                Variable<String> stdout = new Variable();
                
                context("when called without a path", () -> {
                    beforeEach(() -> {
                        stdout.set(runJar("./target/jarinstaller-0.1.0-SNAPSHOT.jar", "uninstall"));
                    }); 
                    
                    it("should say that uninstall needs a path", () -> {
                        assertThat(stdout.get(), containsString("Uninstall action needs a jar file path"));
                    });
                    
                    it("should should print an example usage", () -> {
                        assertThat(stdout.get(), containsString("Like this: jarinstaller uninstall path/to/your.jar"));
                    });
                });
                
                context("the jar has not been installed", () -> {
                    
                    beforeEach(() -> {
                        stdout.set(runJar("./target/jarinstaller-0.1.0-SNAPSHOT.jar", "uninstall", "target/test.jar"));
                    });

                    it("should say that the jar has not been installed", () -> {
                        assertThat(stdout.get(), containsString("There is no test.jar in ~/.jars/jars/"));
                    });
                });
                
                context("target/test.jar is installed", () -> {
                    
                    beforeEach(() -> {
                        runJar("./target/jarinstaller-0.1.0-SNAPSHOT.jar", "install", "target/test.jar");
                        stdout.set(runJar("./target/jarinstaller-0.1.0-SNAPSHOT.jar", "uninstall" , "target/test.jar"));
                    });
                    
                    it("should say that it is removing the jar file", () -> {
                        assertThat(stdout.get(), containsString("Removing ~/.jars/jars/test.jar"));
                    });
                    
                    it("should say that it is removing the bash script", () -> {
                        assertThat(stdout.get(), containsString("Removing ~/.jars/bin/test"));
                    });
                    
                    it("should remove the jar file", () -> {
                        assertThat(new File(DUMMY_HOME+".jars/jars/test.jar").exists(), is(false));
                    });
                    
                    it("should remove the script file", () -> {
                        assertThat(new File(DUMMY_HOME+".jars/bin/test").exists(), is(false));
                    });
                });
            });
        });
    }  
}

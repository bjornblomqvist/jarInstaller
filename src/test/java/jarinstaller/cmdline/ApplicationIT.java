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
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
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
        
        describe("Application run with", () -> {
            
            beforeAll(() -> {
                buildTestJars();
                new File(DUMMY_HOME).mkdirs();
                System.setProperty("user.home", DUMMY_HOME);
            });
        
            afterAll(() -> {
                System.setProperty("user.home", ORIGINAL_HOME);
            });
            
            beforeEach(() -> {
                tryToDelete(new File(DUMMY_HOME + ".jars/jars/").listFiles());
                tryToDelete(new File(DUMMY_HOME + ".jars/jars/"));
                tryToDelete(new File(DUMMY_HOME + ".jars/bin/").listFiles());
                tryToDelete(new File(DUMMY_HOME + ".jars/bin/"));
                tryToDelete(new File(DUMMY_HOME + ".jars/"));
                
                Files.write(new File(DUMMY_HOME + ".profile").toPath(), "".getBytes(), TRUNCATE_EXISTING, CREATE);
            });
            
            context("no params", () -> {
                Variable<String> stdout = new Variable();
                
                beforeEach(() -> {
                    stdout.set(runJar("./target/jarinstaller-0.2.0.jar", ""));
                });
                
                it("should show the help", () -> {
                    assertThat(stdout.get(), containsString("usage: jarinstaller"));
                });
            });
            
            context("unknown params", () -> {
                Variable<String> stdout = new Variable();
                
                beforeEach(() -> {
                    stdout.set(runJar("./target/jarinstaller-0.2.0.jar", "huphup"));
                });
                
                it("should show the help", () -> {
                    assertThat(stdout.get(), containsString("usage: jarinstaller"));
                });
                
                it("should print the known param", () -> {
                    assertThat(stdout.get(), containsString("ERROR! unknown param, \"huphup\""));
                });
            });
            
            describe("--help", () -> {
                
                Variable<String> stdout = new Variable();
                
                beforeEach(() -> {
                    stdout.set(runJar("./target/jarinstaller-0.2.0.jar", "--help"));
                });

                it("should show the help", () -> {
                    assertThat(stdout.get(), containsString("usage: jarinstaller"));
                });
            });
            
            describe("install", () -> {
                Variable<String> stdout = new Variable();
                
                beforeEach(() -> {
                    stdout.set(runJar("./target/jarinstaller-0.2.0.jar", "install"));
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
                    stdout.set(runJar("./target/jarinstaller-0.2.0.jar", "install", "target/test.jar"));
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
                        stdout.set(runJar("./target/jarinstaller-0.2.0.jar", env, "install", "target/test.jar"));
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
                        stdout.set(runJar("./target/jarinstaller-0.2.0.jar", env, "install", "target/test.jar"));
                    });
                    
                    it("should not add a sippet to ~/.profile", () -> {
                        String profileContent = new String(Files.readAllBytes(new File(DUMMY_HOME+".profile").toPath()));
                        assertThat(profileContent, not(containsString("PATH=$PATH:$HOME/.jars/bin # Add jarinstaller bin to PATH")));
                    });
                });
                
                context("we have already written to .profile", () -> {
                    beforeEach(() -> {
                        Files.write(new File(DUMMY_HOME + ".profile").toPath(), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                        HashMap<String, String> env = new HashMap();
                        env.put("PATH", "");
                        stdout.set(runJar("./target/jarinstaller-0.2.0.jar", env, "install", "target/test.jar"));
                        stdout.set(runJar("./target/jarinstaller-0.2.0.jar", env, "install", "target/test.jar"));
                    });
                    
                    it("should not add a sippet to ~/.profile", () -> {
                        String profileContent = new String(Files.readAllBytes(new File(DUMMY_HOME+".profile").toPath()));
                        assertThat(profileContent, not(containsString("PATH=$PATH:$HOME/.jars/bin # Add jarinstaller bin to PATH\n\nPATH=$PATH:$HOME/.jars/bin # Add jarinstaller bin to PATH")));
                    });
                });
            });
            
            describe("--install-self", () -> {
                
                Variable<String> stdout = new Variable();
                
                beforeEach(() -> {
                    stdout.set(runJar("./target/jarinstaller-0.2.0.jar", "--install-self"));
                });
                
                it("should print that it copied the jar", () -> {
                    assertThat(stdout.get(), containsString("Copied self to ~/.jars/jars/jarinstaller-0.2.0.jar"));
                });
               
                it("should print that it created bash script", () -> {
                    assertThat(stdout.get(), containsString("Created bash script ~/.jars/bin/jarinstaller"));
                });
                
                it("should add script file to bin directory", () -> {
                   assertThat(DUMMY_HOME+".jars/bin/jarinstaller does not exist", new File(DUMMY_HOME+".jars/bin/jarinstaller").exists(), is(true));
                }); 
                
                it("should add the jar to the jars directory", () -> {
                   assertThat(DUMMY_HOME+".jars/jars/jarinstaller-0.2.0.jar does not exist", new File(DUMMY_HOME+".jars/jars/jarinstaller-0.2.0.jar").exists(), is(true));
                });
            });
            
            
            
            describe("uninstall", () -> {
                Variable<String> stdout = new Variable();
                
                context("when called without a jar name", () -> {
                    beforeEach(() -> {
                        stdout.set(runJar("./target/jarinstaller-0.2.0.jar", "uninstall"));
                    }); 
                    
                    it("should say that uninstall needs a jar path", () -> {
                        assertThat(stdout.get(), containsString("Uninstall action needs a jar file path"));
                    });
                    
                    it("should should print an example usage", () -> {
                        assertThat(stdout.get(), containsString("Like this: jarinstaller uninstall path/to/your.jar"));
                    });
                });
                
                context("called with a jar name that is not installed", () -> {
                    
                    beforeEach(() -> {
                        stdout.set(runJar("./target/jarinstaller-0.2.0.jar", "uninstall", "test.jar"));
                    });

                    it("should say that the jar has not been installed", () -> {
                        assertThat(stdout.get(), containsString("There is no test.jar in ~/.jars/jars/"));
                    });
                });
                
                context("called with a script name that is not installed", () -> {
                    
                    beforeEach(() -> {
                        stdout.set(runJar("./target/jarinstaller-0.2.0.jar", "uninstall", "test"));
                    });

                    it("should say that there is no such script in bin directory", () -> {
                        assertThat(stdout.get(), containsString("There is no test in ~/.jars/bin/"));
                    });
                });

                context("called with a script name that is installed", () -> {
                    
                    beforeEach(() -> {
                        runJar("./target/jarinstaller-0.2.0.jar", "install", "target/test.jar");
                        stdout.set(runJar("./target/jarinstaller-0.2.0.jar", "uninstall" , "test"));
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
                
                context("called with a jar name that is installed", () -> {
                    
                    beforeEach(() -> {
                        runJar("./target/jarinstaller-0.2.0.jar", "install", "target/test.jar");
                        stdout.set(runJar("./target/jarinstaller-0.2.0.jar", "uninstall" , "test.jar"));
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
            
            describe("--version", () -> {
                
                Variable<String> stdout = new Variable();
                
                beforeEach(() -> {
                    stdout.set(runJar("./target/jarinstaller-0.2.0.jar", "--version"));
                });
                
                it("should show the current version", () -> {
                    assertThat(stdout.get(), containsString("jarinstaller 0.2.0"));
                });
            });
            
            describe("list", () -> {
                
                Variable<String> stdout = new Variable();
                
                context("there are two jars installed", () -> {
                    
                    beforeEach(() -> {
                        runJar("./target/jarinstaller-0.2.0.jar", "install", "target/test.jar");
                        runJar("./target/jarinstaller-0.2.0.jar", "install", "target/test2-1.0.1.jar");
                        stdout.set(runJar("./target/jarinstaller-0.2.0.jar", "list"));
                    });
                    
                    it("should show the two installed jars", () -> {
                        assertThat(stdout.get(), containsString("test  -> test.jar"));
                        assertThat(stdout.get(), containsString("test2 -> test2-1.0.1.jar"));
                    });
                });
            });
        });
    }  
}

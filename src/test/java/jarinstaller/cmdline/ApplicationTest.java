package jarinstaller.cmdline;

import com.greghaskins.spectrum.Block;
import com.greghaskins.spectrum.Spectrum;
import com.greghaskins.spectrum.Variable;
import static com.greghaskins.spectrum.dsl.specification.Specification.afterAll;
import static com.greghaskins.spectrum.dsl.specification.Specification.beforeAll;
import static com.greghaskins.spectrum.dsl.specification.Specification.beforeEach;
import static com.greghaskins.spectrum.dsl.specification.Specification.context;
import static com.greghaskins.spectrum.dsl.specification.Specification.it;
import static com.greghaskins.spectrum.dsl.specification.Specification.describe;
import static jarinstaller.JarInstallerTest.buildTestJars;
import static jarinstaller.JarInstallerTest.tryToDelete;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Paths;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.runner.RunWith;

@RunWith(Spectrum.class)
public class ApplicationTest {
    
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
            });
            
            context("no params", () -> {
                Variable<String> stdout = new Variable();
                
                beforeEach(() -> {
                    stdout.set(captureOutput(() -> {
                        Application.main("");
                    }));
                });
                
                it("should show the help", () -> {
                    assertThat(stdout.get(), containsString("usage: jarInstaller"));
                });
            });
            
            describe("--help", () -> {
                
                Variable<String> stdout = new Variable();
                
                beforeEach(() -> {
                    stdout.set(captureOutput(() -> {
                        Application.main("--help");
                    }));
                });

                it("should show the help", () -> {
                    assertThat(stdout.get(), containsString("usage: jarInstaller"));
                });
            });
            
            describe("install", () -> {
                Variable<String> stdout = new Variable();
                
                beforeEach(() -> {
                    stdout.set(captureOutput(() -> {
                        Application.main("install");
                    }));
                });
                
                it("should print that it needs a jar file path", () -> {
                    assertThat(stdout.get(), containsString("Install action needs a jar file path."));
                });
                
                it("should print an example", () -> {
                    assertThat(stdout.get(), containsString("jarInstaller install path/to/your.jar"));
                });
            });
            
            describe("install target/test.jar", () -> {
                
                Variable<String> stdout = new Variable();
                
                beforeEach(() -> {
                    stdout.set(captureOutput(() -> {
                        Application.main("install", "target/test.jar");
                    }));
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
            });
            
            describe("uninstall target/test.jar", () -> {
                Variable<String> stdout = new Variable();
                
                context("when called without a path", () -> {
                    beforeEach(() -> {
                        stdout.set(captureOutput(() -> {
                            Application.main("uninstall");
                        }));
                    }); 
                    
                    it("should say that uninstall needs a path", () -> {
                        assertThat(stdout.get(), containsString("Uninstall action needs a jar file path"));
                    });
                    
                    it("should should print an example usage", () -> {
                        assertThat(stdout.get(), containsString("Like this: jarInstaller uninstall path/to/your.jar"));
                    });
                });
                
                context("the jar has not been installed", () -> {
                    
                    beforeEach(() -> {
                        stdout.set(captureOutput(() -> {
                            Application.main("uninstall", "target/test.jar");
                        }));
                    });

                    it("should say that the jar has not been installed", () -> {
                        assertThat(stdout.get(), containsString("There is no test.jar in ~/.jars/jars/"));
                    });
                });
                
                context("target/test.jar is installed", () -> {
                    
                    beforeEach(() -> {
                        Application.main("install", "target/test.jar");
                        stdout.set(captureOutput(() -> {
                            Application.main("uninstall", "target/test.jar");
                        }));
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
    
    private static String captureOutput(Block block) throws Throwable {
        PrintStream currentStdout = System.out;
        PrintStream currentErrout = System.err;
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream newOut = new PrintStream(baos);
            System.setOut(newOut);
            System.setErr(newOut);
            
            block.run();
            
            return baos.toString();
        } finally {
            System.setOut(currentStdout);
            System.setErr(currentErrout);
        }
    }
}

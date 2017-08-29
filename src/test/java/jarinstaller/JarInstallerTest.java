package jarinstaller;

import com.greghaskins.spectrum.Spectrum;
import com.greghaskins.spectrum.Variable;
import static com.greghaskins.spectrum.dsl.specification.Specification.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.runner.RunWith;

@RunWith(Spectrum.class)
public class JarInstallerTest {
    
    public static String runJar(String jarPath, String... args) throws IOException {
        
        List<String> commands = new ArrayList();
        commands.add("java");
        commands.add("-jar");
        commands.add(jarPath);
        commands.addAll(Arrays.asList(args));
        
        ProcessBuilder builder = new ProcessBuilder(commands.toArray(new String[commands.size()]));
        builder.environment().put("HOME", System.getProperty("user.home"));
        Process process = builder.start();
        
        InputStreamReader isr = new InputStreamReader(process.getErrorStream());
        BufferedReader br = new BufferedReader(isr);
        
        String result = "";
        
        String line = "";
        while ((line = br.readLine()) != null) {
            result += line + "\n";
        }
        
        isr = new InputStreamReader(process.getInputStream());
        br = new BufferedReader(isr);
        
        while ((line = br.readLine()) != null) {
            result += line + "\n";
        }

        return result;
    }
    
    public static String runClassInJar(String jarPath, String className, String... args) throws IOException {
        List<String> commands = new ArrayList();
        commands.add("java");
        commands.add("-cp");
        commands.add(jarPath);
        commands.add(className);
        commands.addAll(Arrays.asList(args));
        
        ProcessBuilder builder = new ProcessBuilder(commands.toArray(new String[commands.size()]));
        builder.environment().put("HOME", System.getProperty("user.home"));
        Process process = builder.start();
        
        InputStreamReader isr = new InputStreamReader(process.getErrorStream());
        BufferedReader br = new BufferedReader(isr);
        
        String result = "";
        
        String line = "";
        while ((line = br.readLine()) != null) {
            result += line + "\n";
        }
        
        isr = new InputStreamReader(process.getInputStream());
        br = new BufferedReader(isr);
        
        while ((line = br.readLine()) != null) {
            result += line + "\n";
        }

        return result;
    }
    
    public static String runScript(String scriptPath, String... args) throws IOException {
        List<String> commands = new ArrayList();
        commands.add("/bin/bash");
        commands.add(scriptPath);
        commands.addAll(Arrays.asList(args));
        
        ProcessBuilder builder = new ProcessBuilder(commands.toArray(new String[commands.size()]));
        builder.environment().put("HOME", System.getProperty("user.home"));
        Process process = builder.start();
        
        InputStreamReader isr = new InputStreamReader(process.getErrorStream());
        BufferedReader br = new BufferedReader(isr);
        
        String result = "";
        
        String line = "";
        while ((line = br.readLine()) != null) {
            result += line + "\n";
        }
        
        isr = new InputStreamReader(process.getInputStream());
        br = new BufferedReader(isr);
        
        while ((line = br.readLine()) != null) {
            result += line + "\n";
        }

        return result;
    }
    
    public static void tryToDelete(File file) {
        try {
            file.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
            
    }
    
    public static void buildTestJars() throws IOException, InterruptedException {
        {
            File test_jar = new File("./target/test.jar");
            if (test_jar.exists()) {
                test_jar.delete();
            }

            Process process = new ProcessBuilder("jar", "cvfm", "./target/test.jar", "./src/test/resources/MANIFEST.MF", "-C", "./target/classes", "jarinstaller").start();
            process.waitFor();

            process = new ProcessBuilder("jar", "uvf", "./target/test.jar", "-C", "./target/test-classes", "jarinstaller/TestMain.class").start();
            process.waitFor();
        }
        
        {
            File test_jar = new File("./target/test-without-main.jar");
            if (test_jar.exists()) {
                test_jar.delete();
            }

            Process process = new ProcessBuilder("jar", "cvf", "./target/test-without-main.jar", "-C", "./target/classes", "jarinstaller").start();
            process.waitFor();

            process = new ProcessBuilder("jar", "uvf", "./target/test-without-main.jar", "-C", "./target/test-classes", "jarinstaller/TestMain.class").start();
            process.waitFor();
        }
    }
    
    {

    String cwd = Paths.get(".").toAbsolutePath().normalize().toString();
    String ORIGINAL_HOME = System.getProperty("user.home");
    String DUMMY_HOME = cwd + "/target/dummy_home/";
    
    describe("JarInstaller", () -> {
        
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
        
        describe(".unUinstall()", () -> {
            context("is called in a jar that has been installed but is not run in the installed jar", () -> {
               
                Variable<String> result = new Variable();
                
                beforeEach(() -> {
                    runJar("./target/test.jar", "install");
                    result.set(runJar("./target/test.jar", "unInstall"));
                });
               
                it("should return true", () -> {
                   assertThat(result.get(), containsString("true"));
                });
                
                it("should remove the installed jar", () -> {
                   assertThat(new File(DUMMY_HOME+".jars/jars/test.jar").exists(), is(false));
                });
                
                it("should remove the shell script", () -> {
                   assertThat(new File(DUMMY_HOME+".jars/bin/test").exists(), is(false));
                });
            });
           
            context("is called in a installed jar", () -> {
               
                Variable<String> result = new Variable();
                
                beforeEach(() -> {
                    runJar("./target/test.jar", "install");
                    result.set(runScript("./target/dummy_home/.jars/bin/test", "unInstall"));
                });
               
                it("should return true", () -> {
                   assertThat(result.get(), containsString("true"));
                });
                
                it("should remove the installed jar", () -> {
                   assertThat(new File(DUMMY_HOME+".jars/jars/test.jar").exists(), is(false));
                });
                
                it("should remove the shell script", () -> {
                   assertThat(new File(DUMMY_HOME+".jars/bin/test").exists(), is(false));
                });
            });
           
            context("is called in a jar that has not been installed", () -> {
                
                Variable<String> result = new Variable();
                
                beforeEach(() -> {
                    result.set(runJar("./target/test.jar", "unInstall"));
                });
               
                it("should return false", () -> {
                   assertThat(result.get(), containsString("false"));
                });
            });
            
            context("is called from jar without Main-Class: in manifest", () -> {
                
                Variable<String> result = new Variable();
                
                beforeEach(() -> {
                    result.set(runClassInJar("./target/test-without-main.jar", "jarinstaller.TestMain", "uninstall"));
                });
                
                it("should return false", () -> {
                   assertThat(result.get(), containsString("false"));
                });
            });
        });
        
        describe(".install()", () -> {
            context("is called in a jar that has been installed but is not run in the installed jar", () -> {
               
                Variable<String> result = new Variable();
                
                beforeEach(() -> {
                    runJar("./target/test.jar", "install");
                    result.set(runJar("./target/test.jar", "install"));
                });
               
                it("should return true", () -> {
                   assertThat(result.get(), containsString("true"));
                });
            });
            
            context("is called in a installed jar", () -> {
               
                Variable<String> result = new Variable();
                
                beforeEach(() -> {
                    runJar("./target/test.jar", "install");
                    result.set(runJar("./target/dummy_home/.jars/jars/test.jar", "install"));
                });
               
                it("should return true", () -> {
                   assertThat(result.get(), containsString("true"));
                });
            });
           
            context("is called in a jar that has not been installed", () -> {
               
                Variable<String> result = new Variable();
                
                beforeEach(() -> {
                    result.set(runJar("./target/test.jar", "install"));
                });
                
                it("should return true", () -> {
                    assertThat(result.get(), containsString("true"));
                });
               
                it("should add the jar to the jars directory", () -> {
                   assertThat(new File(DUMMY_HOME+".jars/jars/test.jar").exists(), is(true));
                });
               
                it("should add script file to bin directory", () -> {
                   assertThat(new File(DUMMY_HOME+".jars/bin/test").exists(), is(true));
                });
            });
            
            context("is called from outside of a jar", () -> {
                it("should throw an exception", () -> {
                    Exception exception = null;
                    try {
                        JarInstaller.install(null);
                    } catch (Exception ex) {
                        exception = ex;
                    }
                    assertThat(exception, is(not(equalTo(null))));
                });
            });
            
            context("is called from jar without Main-Class: in manifest", () -> {
                
                Variable<String> result = new Variable();
                
                beforeAll(() -> {
                    result.set(runClassInJar("./target/test-without-main.jar", "jarinstaller.TestMain", "install"));
                });
                
                it("should throw an exception", () -> {
                   assertThat(result.get(), containsString("at jarinstaller.TestMain.main("));
                   assertThat(result.get(), containsString("Jar file Manifest does not have Main-class"));
                });
            });
        });
        
        describe(".isInJarsDirectory()", () -> {
            context("is called with a jar path that has been installed but is not run in the installed jar", () -> {
               
                Variable<String> result = new Variable();
                
                beforeEach(() -> {
                    runJar("./target/test.jar", "install");
                    result.set(runJar("./target/test.jar", "isInJarsDirectory"));
                });
                
                it("should return false", () -> {
                   assertThat(result.get(), containsString("false"));
                });
            });
           
           context("is called with a jar path to the jars direcotry", () -> {
               
                Variable<String> result = new Variable();
                
                beforeEach(() -> {
                    runJar("./target/test.jar", "install");
                    result.set(runScript("./target/dummy_home/.jars/bin/test", "isInJarsDirectory"));
                });
               
                it("should return true", () -> {
                   assertThat(result.get(), containsString("true"));
                });
            });
           
            context("is called in a jar that has not been installed", () -> {
                
                Variable<String> result = new Variable();
                
                beforeEach(() -> {
                    result.set(runJar("./target/test.jar", "isInJarsDirectory"));
                });
                
                it("should return false", () -> {
                   assertThat(result.get(), containsString("false"));
                });
            });
        });
        
        describe(".isInstalled()", () -> {    
            context("is called in a jar that has been installed but is not run in the installed jar", () -> {
               
                Variable<String> result = new Variable();
                
                beforeEach(() -> {
                    runJar("./target/test.jar", "install");
                    result.set(runJar("./target/test.jar", "isInstalled"));
                });
               
                it("should return true", () -> {
                   assertThat(result.get(), containsString("true"));
                });
           });
           
            context("is called in a installed jar", () -> {
               
                Variable<String> result = new Variable();
                
                beforeEach(() -> {
                    runJar("./target/test.jar", "install");
                    result.set(runScript("./target/dummy_home/.jars/bin/test", "isInstalled"));
                });
               
                it("should return true", () -> {
                   assertThat(result.get(), containsString("true"));
                });
            });
           
            context("is called in a jar that has not been installed", () -> {
               
                Variable<String> result = new Variable();
                
                beforeEach(() -> {
                    result.set(runJar("./target/test.jar", "isInstalled"));
                });
               
                it("should return false", () -> {
                   assertThat(result.get(), containsString("false"));
                });
            });
        });
    });
    
}}

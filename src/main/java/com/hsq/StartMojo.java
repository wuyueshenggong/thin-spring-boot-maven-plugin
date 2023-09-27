//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ConnectException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.management.MBeanServerConnection;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.springframework.boot.loader.tools.JavaExecutable;
import org.springframework.boot.loader.tools.RunProcess;


@Mojo(
    name = "start",
    requiresProject = true,
    defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST,
    requiresDependencyResolution = ResolutionScope.TEST
)
public class StartMojo extends AbstractRunMojo {
    private static final String ENABLE_MBEAN_PROPERTY = "--spring.application.admin.enabled=true";
    private static final String JMX_NAME_PROPERTY_PREFIX = "--spring.application.admin.jmx-name=";
    @Parameter
    private String jmxName = "org.springframework.boot:type=Admin,name=SpringApplication";
    @Parameter
    private int jmxPort = 9001;
    @Parameter
    private long wait = 500L;
    @Parameter
    private int maxAttempts = 60;
    private final Object lock = new Object();

    public StartMojo() {
    }

    @Override
    protected void runWithForkedJvm(File workingDirectory, List<String> args, Map<String, String> environmentVariables) throws MojoExecutionException, MojoFailureException {
        RunProcess runProcess = this.runProcess(workingDirectory, args, environmentVariables);

        try {
            this.waitForSpringApplication();
        } catch (MojoFailureException | MojoExecutionException var6) {
            runProcess.kill();
            throw var6;
        }
    }

    private RunProcess runProcess(File workingDirectory, List<String> args, Map<String, String> environmentVariables) throws MojoExecutionException {
        try {
            RunProcess runProcess = new RunProcess(workingDirectory, new String[]{(new JavaExecutable()).toString()});
            runProcess.run(false, args, environmentVariables);
            return runProcess;
        } catch (Exception var5) {
            throw new MojoExecutionException("Could not exec java", var5);
        }
    }

	@Override
    protected RunArguments resolveApplicationArguments() {
        RunArguments applicationArguments = super.resolveApplicationArguments();
        applicationArguments.getArgs().addLast("--spring.application.admin.enabled=true");
        if (this.isFork()) {
            applicationArguments.getArgs().addLast("--spring.application.admin.jmx-name=" + this.jmxName);
        }

        return applicationArguments;
    }

	@Override
    protected RunArguments resolveJvmArguments() {
        RunArguments jvmArguments = super.resolveJvmArguments();
        if (this.isFork()) {
            List<String> remoteJmxArguments = new ArrayList();
            remoteJmxArguments.add("-Dcom.sun.management.jmxremote");
            remoteJmxArguments.add("-Dcom.sun.management.jmxremote.port=" + this.jmxPort);
            remoteJmxArguments.add("-Dcom.sun.management.jmxremote.authenticate=false");
            remoteJmxArguments.add("-Dcom.sun.management.jmxremote.ssl=false");
            remoteJmxArguments.add("-Djava.rmi.server.hostname=127.0.0.1");
            jvmArguments.getArgs().addAll(remoteJmxArguments);
        }

        return jvmArguments;
    }

	@Override
    protected void runWithMavenJvm(String startClassName, String... arguments) throws MojoExecutionException {
        IsolatedThreadGroup threadGroup = new IsolatedThreadGroup( startClassName);
        Thread launchThread = new Thread(threadGroup, new LaunchRunner( startClassName, arguments), startClassName + ".main()");
        launchThread.setContextClassLoader(new URLClassLoader(this.getClassPathUrls()));
        launchThread.start();
        this.waitForSpringApplication(this.wait, this.maxAttempts);
    }

    private void waitForSpringApplication(long wait, int maxAttempts) throws MojoExecutionException {
        SpringApplicationAdminClient client = new SpringApplicationAdminClient(ManagementFactory.getPlatformMBeanServer(), this.jmxName);
        this.getLog().debug("Waiting for spring application to start...");

        for(int i = 0; i < maxAttempts; ++i) {
            if (client.isReady()) {
                return;
            }

            String message = "Spring application is not ready yet, waiting " + wait + "ms (attempt " + (i + 1) + ")";
            this.getLog().debug(message);
            synchronized(this.lock) {
                try {
                    this.lock.wait(wait);
                } catch (InterruptedException var10) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for Spring Boot app to start.");
                }
            }
        }

        throw new MojoExecutionException("Spring application did not start before the configured timeout (" + wait * (long)maxAttempts + "ms");
    }

    private void waitForSpringApplication() throws MojoFailureException, MojoExecutionException {
        try {
            if (this.isFork()) {
                this.waitForForkedSpringApplication();
            } else {
                this.doWaitForSpringApplication(ManagementFactory.getPlatformMBeanServer());
            }

        } catch (IOException var2) {
            throw new MojoFailureException("Could not contact Spring Boot application", var2);
        } catch (Exception var3) {
            throw new MojoExecutionException("Could not figure out if the application has started", var3);
        }
    }

    private void waitForForkedSpringApplication() throws IOException, MojoFailureException, MojoExecutionException {
        try {
            this.getLog().debug("Connecting to local MBeanServer at port " + this.jmxPort);
            JMXConnector connector = (JMXConnector)this.execute(this.wait, this.maxAttempts, new StartMojo.CreateJmxConnector(this.jmxPort));
            Throwable var2 = null;

            try {
                if (connector == null) {
                    throw new MojoExecutionException("JMX MBean server was not reachable before the configured timeout (" + this.wait * (long)this.maxAttempts + "ms");
                }

                this.getLog().debug("Connected to local MBeanServer at port " + this.jmxPort);
                MBeanServerConnection connection = connector.getMBeanServerConnection();
                this.doWaitForSpringApplication(connection);
            } catch (Throwable var13) {
                var2 = var13;
                throw var13;
            } finally {
                if (connector != null) {
                    if (var2 != null) {
                        try {
                            connector.close();
                        } catch (Throwable var12) {
                            var2.addSuppressed(var12);
                        }
                    } else {
                        connector.close();
                    }
                }

            }

        } catch (IOException var15) {
            throw var15;
        } catch (Exception var16) {
            throw new MojoExecutionException("Failed to connect to MBean server at port " + this.jmxPort, var16);
        }
    }

    private void doWaitForSpringApplication(MBeanServerConnection connection) throws IOException, MojoExecutionException, MojoFailureException {
        SpringApplicationAdminClient client = new SpringApplicationAdminClient(connection, this.jmxName);

        try {
            this.execute(this.wait, this.maxAttempts, () -> {
                return client.isReady() ? true : null;
            });
        } catch (ReflectionException var4) {
            throw new MojoExecutionException("Unable to retrieve 'ready' attribute", var4.getCause());
        } catch (Exception var5) {
            throw new MojoFailureException("Could not invoke shutdown operation", var5);
        }
    }

    public <T> T execute(long wait, int maxAttempts, Callable<T> callback) throws Exception {
        this.getLog().debug("Waiting for spring application to start...");

        for(int i = 0; i < maxAttempts; ++i) {
            T result = callback.call();
            if (result != null) {
                return result;
            }

            String message = "Spring application is not ready yet, waiting " + wait + "ms (attempt " + (i + 1) + ")";
            this.getLog().debug(message);
            synchronized(this.lock) {
                try {
                    this.lock.wait(wait);
                } catch (InterruptedException var11) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for Spring Boot app to start.");
                }
            }
        }

        throw new MojoExecutionException("Spring application did not start before the configured timeout (" + wait * (long)maxAttempts + "ms");
    }

    private class CreateJmxConnector implements Callable<JMXConnector> {
        private final int port;

        CreateJmxConnector(int port) {
            this.port = port;
        }

	    @Override
        public JMXConnector call() throws Exception {
            try {
                return SpringApplicationAdminClient.connect(this.port);
            } catch (IOException var3) {
                if (this.hasCauseWithType(var3, ConnectException.class)) {
                    String message = "MBean server at port " + this.port + " is not up yet...";
                    StartMojo.this.getLog().debug(message);
                    return null;
                } else {
                    throw var3;
                }
            }
        }

        private boolean hasCauseWithType(Throwable t, Class<? extends Exception> type) {
            return type.isAssignableFrom(t.getClass()) || t.getCause() != null && this.hasCauseWithType(t.getCause(), type);
        }
    }
}

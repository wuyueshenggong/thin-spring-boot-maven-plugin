//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.springframework.boot.loader.tools.JavaExecutable;
import org.springframework.boot.loader.tools.RunProcess;


@Mojo(
    name = "run",
    requiresProject = true,
    defaultPhase = LifecyclePhase.VALIDATE,
    requiresDependencyResolution = ResolutionScope.TEST
)
@Execute(
    phase = LifecyclePhase.TEST_COMPILE
)
public class RunMojo extends AbstractRunMojo {
    private static final int EXIT_CODE_SIGINT = 130;
    private static final String RESTARTER_CLASS_LOCATION = "org/springframework/boot/devtools/restart/Restarter.class";
    private Boolean hasDevtools;

    public RunMojo() {
    }
    @Override
    protected boolean enableForkByDefault() {
        return super.enableForkByDefault() || this.hasDevtools();
    }
    @Override
    protected void logDisabledFork() {
        super.logDisabledFork();
        if (this.hasDevtools()) {
            this.getLog().warn("Fork mode disabled, devtools will be disabled");
        }

    }
    @Override
    protected void runWithForkedJvm(File workingDirectory, List<String> args, Map<String, String> environmentVariables) throws MojoExecutionException {
        try {
            RunProcess runProcess = new RunProcess(workingDirectory, new String[]{(new JavaExecutable()).toString()});
            Runtime.getRuntime().addShutdownHook(new Thread(new RunMojo.RunProcessKiller(runProcess)));
            int exitCode = runProcess.run(true, args, environmentVariables);
            if (exitCode != 0 && exitCode != 130) {
                throw new MojoExecutionException("Application finished with exit code: " + exitCode);
            }
        } catch (Exception var6) {
            throw new MojoExecutionException("Could not exec java", var6);
        }
    }
    @Override
    protected void runWithMavenJvm(String startClassName, String... arguments) throws MojoExecutionException {
        IsolatedThreadGroup threadGroup = new IsolatedThreadGroup(startClassName);
        Thread launchThread = new Thread(threadGroup, new LaunchRunner( startClassName, arguments), "main");
        launchThread.setContextClassLoader(new URLClassLoader(this.getClassPathUrls()));
        launchThread.start();
        this.join(threadGroup);
        threadGroup.rethrowUncaughtException();
    }

    private void join(ThreadGroup threadGroup) {
        boolean hasNonDaemonThreads;
        do {
            hasNonDaemonThreads = false;
            Thread[] threads = new Thread[threadGroup.activeCount()];
            threadGroup.enumerate(threads);
            Thread[] var4 = threads;
            int var5 = threads.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                Thread thread = var4[var6];
                if (thread != null && !thread.isDaemon()) {
                    try {
                        hasNonDaemonThreads = true;
                        thread.join();
                    } catch (InterruptedException var9) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } while(hasNonDaemonThreads);

    }

    private boolean hasDevtools() {
        if (this.hasDevtools == null) {
            this.hasDevtools = this.checkForDevtools();
        }

        return this.hasDevtools;
    }

    private boolean checkForDevtools() {
        try {
            URL[] urls = this.getClassPathUrls();
            URLClassLoader classLoader = new URLClassLoader(urls);
            Throwable var3 = null;

            boolean var4;
            try {
                var4 = classLoader.findResource("org/springframework/boot/devtools/restart/Restarter.class") != null;
            } catch (Throwable var14) {
                var3 = var14;
                throw var14;
            } finally {
                if (classLoader != null) {
                    if (var3 != null) {
                        try {
                            classLoader.close();
                        } catch (Throwable var13) {
                            var3.addSuppressed(var13);
                        }
                    } else {
                        classLoader.close();
                    }
                }

            }

            return var4;
        } catch (Exception var16) {
            return false;
        }
    }

    private static final class RunProcessKiller implements Runnable {
        private final RunProcess runProcess;

        private RunProcessKiller(RunProcess runProcess) {
            this.runProcess = runProcess;
        }
        @Override
        public void run() {
            this.runProcess.kill();
        }
    }
}

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.collection.AbstractArtifactFeatureFilter;
import org.apache.maven.shared.artifact.filter.collection.ArtifactsFilter;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;
import org.springframework.boot.loader.tools.FileUtils;
import org.springframework.boot.loader.tools.MainClassFinder;

public abstract class AbstractRunMojo extends AbstractDependencyFilterMojo {
    private static final String SPRING_BOOT_APPLICATION_CLASS_NAME = "org.springframework.boot.autoconfigure.SpringBootApplication";
    @Parameter(
        defaultValue = "${project}",
        readonly = true,
        required = true
    )
    private MavenProject project;
    @Parameter(
        property = "spring-boot.run.addResources",
        defaultValue = "false"
    )
    private boolean addResources = false;
    @Parameter(
        property = "spring-boot.run.agent"
    )
    private File[] agent;
    @Parameter(
        property = "spring-boot.run.noverify"
    )
    private boolean noverify = false;
    @Parameter(
        property = "spring-boot.run.workingDirectory"
    )
    private File workingDirectory;
    @Parameter(
        property = "spring-boot.run.jvmArguments"
    )
    private String jvmArguments;
    @Parameter
    private Map<String, String> systemPropertyVariables;
    @Parameter
    private Map<String, String> environmentVariables;
    @Parameter(
        property = "spring-boot.run.arguments"
    )
    private String[] arguments;
    @Parameter(
        property = "spring-boot.run.profiles"
    )
    private String[] profiles;
    @Parameter(
        property = "spring-boot.run.main-class"
    )
    private String mainClass;
    @Parameter(
        property = "spring-boot.run.folders"
    )
    private String[] folders;
    @Parameter(
        defaultValue = "${project.build.outputDirectory}",
        required = true
    )
    private File classesDirectory;
    @Parameter(
        property = "spring-boot.run.fork"
    )
    private Boolean fork;
    @Parameter(
        property = "spring-boot.run.useTestClasspath",
        defaultValue = "false"
    )
    private Boolean useTestClasspath;
    @Parameter(
        property = "spring-boot.run.skip",
        defaultValue = "false"
    )
    private boolean skip;

    public AbstractRunMojo() {
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.skip) {
            this.getLog().debug("skipping run as per configuration.");
        } else {
            this.run(this.getStartClass());
        }
    }

    protected boolean isFork() {
        return Boolean.TRUE.equals(this.fork) || this.fork == null && this.enableForkByDefault();
    }

    protected boolean enableForkByDefault() {
        return this.hasAgent() || this.hasJvmArgs() || this.hasEnvVariables() || this.hasWorkingDirectorySet();
    }

    private boolean hasAgent() {
        return this.agent != null && this.agent.length > 0;
    }

    private boolean hasJvmArgs() {
        return this.jvmArguments != null && !this.jvmArguments.isEmpty() || this.systemPropertyVariables != null && !this.systemPropertyVariables.isEmpty();
    }

    private boolean hasEnvVariables() {
        return this.environmentVariables != null && !this.environmentVariables.isEmpty();
    }

    private boolean hasWorkingDirectorySet() {
        return this.workingDirectory != null;
    }

    private void run(String startClassName) throws MojoExecutionException, MojoFailureException {
        boolean fork = this.isFork();
        this.project.getProperties().setProperty("_spring.boot.fork.enabled", Boolean.toString(fork));
        if (fork) {
            this.doRunWithForkedJvm(startClassName);
        } else {
            this.logDisabledFork();
            this.runWithMavenJvm(startClassName, this.resolveApplicationArguments().asArray());
        }

    }

    protected void logDisabledFork() {
        if (this.getLog().isWarnEnabled()) {
            if (this.hasAgent()) {
                this.getLog().warn("Fork mode disabled, ignoring agent");
            }

            if (this.hasJvmArgs()) {
                RunArguments runArguments = this.resolveJvmArguments();
                this.getLog().warn("Fork mode disabled, ignoring JVM argument(s) [" + (String)Arrays.stream(runArguments.asArray()).collect(Collectors.joining(" ")) + "]");
            }

            if (this.hasWorkingDirectorySet()) {
                this.getLog().warn("Fork mode disabled, ignoring working directory configuration");
            }
        }

    }

    private void doRunWithForkedJvm(String startClassName) throws MojoExecutionException, MojoFailureException {
        List<String> args = new ArrayList();
        this.addAgents(args);
        this.addJvmArgs(args);
        this.addClasspath(args);
        args.add(startClassName);
        this.addArgs(args);
        this.runWithForkedJvm(this.workingDirectory != null ? this.workingDirectory : this.project.getBasedir(), args, this.determineEnvironmentVariables());
    }

    protected abstract void runWithForkedJvm(File workingDirectory, List<String> args, Map<String, String> environmentVariables) throws MojoExecutionException, MojoFailureException;

    protected abstract void runWithMavenJvm(String startClassName, String... arguments) throws MojoExecutionException, MojoFailureException;

    protected RunArguments resolveApplicationArguments() {
        RunArguments runArguments = new RunArguments(this.arguments);
        this.addActiveProfileArgument(runArguments);
        return runArguments;
    }

    protected EnvVariables resolveEnvVariables() {
        return new EnvVariables(this.environmentVariables);
    }

    private void addArgs(List<String> args) {
        RunArguments applicationArguments = this.resolveApplicationArguments();
        Collections.addAll(args, applicationArguments.asArray());
        this.logArguments("Application argument(s): ", this.arguments);
    }

    private Map<String, String> determineEnvironmentVariables() {
        EnvVariables envVariables = this.resolveEnvVariables();
        this.logArguments("Environment variable(s): ", envVariables.asArray());
        return envVariables.asMap();
    }

    protected RunArguments resolveJvmArguments() {
        StringBuilder stringBuilder = new StringBuilder();
        if (this.systemPropertyVariables != null) {
            stringBuilder.append((String)this.systemPropertyVariables.entrySet().stream().map((e) -> {
                return AbstractRunMojo.SystemPropertyFormatter.format((String)e.getKey(), (String)e.getValue());
            }).collect(Collectors.joining(" ")));
        }

        if (this.jvmArguments != null) {
            stringBuilder.append(" ").append(this.jvmArguments);
        }

        return new RunArguments(stringBuilder.toString());
    }

    private void addJvmArgs(List<String> args) {
        RunArguments jvmArguments = this.resolveJvmArguments();
        Collections.addAll(args, jvmArguments.asArray());
        this.logArguments("JVM argument(s): ", jvmArguments.asArray());
    }

    private void addAgents(List<String> args) {
        if (this.agent != null) {
            if (this.getLog().isInfoEnabled()) {
                this.getLog().info("Attaching agents: " + Arrays.asList(this.agent));
            }

            File[] var2 = this.agent;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                File agent = var2[var4];
                args.add("-javaagent:" + agent);
            }
        }

        if (this.noverify) {
            args.add("-noverify");
        }

    }

    private void addActiveProfileArgument(RunArguments arguments) {
        if (this.profiles.length > 0) {
            StringBuilder arg = new StringBuilder("--spring.profiles.active=");

            for(int i = 0; i < this.profiles.length; ++i) {
                arg.append(this.profiles[i]);
                if (i < this.profiles.length - 1) {
                    arg.append(",");
                }
            }

            arguments.getArgs().addFirst(arg.toString());
            this.logArguments("Active profile(s): ", this.profiles);
        }

    }

    private void addClasspath(List<String> args) throws MojoExecutionException {
        try {
            StringBuilder classpath = new StringBuilder();
            URL[] var3 = this.getClassPathUrls();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                URL ele = var3[var5];
                if (classpath.length() > 0) {
                    classpath.append(File.pathSeparator);
                }

                classpath.append(new File(ele.toURI()));
            }

            if (this.getLog().isDebugEnabled()) {
                this.getLog().debug("Classpath for forked process: " + classpath);
            }

            args.add("-cp");
            args.add(classpath.toString());
        } catch (Exception var7) {
            throw new MojoExecutionException("Could not build classpath", var7);
        }
    }

    private String getStartClass() throws MojoExecutionException {
        String mainClass = this.mainClass;
        if (mainClass == null) {
            try {
                mainClass = MainClassFinder.findSingleMainClass(this.classesDirectory, "org.springframework.boot.autoconfigure.SpringBootApplication");
            } catch (IOException var3) {
                throw new MojoExecutionException(var3.getMessage(), var3);
            }
        }

        if (mainClass == null) {
            throw new MojoExecutionException("Unable to find a suitable main class, please add a 'mainClass' property");
        } else {
            return mainClass;
        }
    }

    protected URL[] getClassPathUrls() throws MojoExecutionException {
        try {
            List<URL> urls = new ArrayList();
            this.addUserDefinedFolders(urls);
            this.addResources(urls);
            this.addProjectClasses(urls);
            this.addDependencies(urls);
            return (URL[])urls.toArray(new URL[0]);
        } catch (IOException var2) {
            throw new MojoExecutionException("Unable to build classpath", var2);
        }
    }

    private void addUserDefinedFolders(List<URL> urls) throws MalformedURLException {
        if (this.folders != null) {
            String[] var2 = this.folders;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String folder = var2[var4];
                urls.add((new File(folder)).toURI().toURL());
            }
        }

    }

    private void addResources(List<URL> urls) throws IOException {
        if (this.addResources) {
            Iterator var2 = this.project.getResources().iterator();

            while(var2.hasNext()) {
                Resource resource = (Resource)var2.next();
                File directory = new File(resource.getDirectory());
                urls.add(directory.toURI().toURL());
                FileUtils.removeDuplicatesFromOutputDirectory(this.classesDirectory, directory);
            }
        }

    }

    private void addProjectClasses(List<URL> urls) throws MalformedURLException {
        urls.add(this.classesDirectory.toURI().toURL());
    }

    private void addDependencies(List<URL> urls) throws MalformedURLException, MojoExecutionException {
        FilterArtifacts filters = this.useTestClasspath ? this.getFilters(new ArtifactsFilter[0]) : this.getFilters(new ArtifactsFilter[]{new AbstractRunMojo.TestArtifactFilter()});
        Set<Artifact> artifacts = this.filterDependencies(this.project.getArtifacts(), filters);
        Iterator var4 = artifacts.iterator();

        while(var4.hasNext()) {
            Artifact artifact = (Artifact)var4.next();
            if (artifact.getFile() != null) {
                urls.add(artifact.getFile().toURI().toURL());
            }
        }

    }

    private void logArguments(String message, String[] args) {
        if (this.getLog().isDebugEnabled()) {
            this.getLog().debug((CharSequence)Arrays.stream(args).collect(Collectors.joining(" ", message, "")));
        }

    }

    static class SystemPropertyFormatter {
        SystemPropertyFormatter() {
        }

        public static String format(String key, String value) {
            if (key == null) {
                return "";
            } else {
                return value != null && !value.isEmpty() ? String.format("-D%s=\"%s\"", key, value) : String.format("-D%s", key);
            }
        }
    }

    class LaunchRunner implements Runnable {
        private final String startClassName;
        private final String[] args;

        LaunchRunner(String startClassName, String... args) {
            this.startClassName = startClassName;
            this.args = args != null ? args : new String[0];
        }


        @Override
        public void run() {
            Thread thread = Thread.currentThread();
            ClassLoader classLoader = thread.getContextClassLoader();

            try {
                Class<?> startClass = classLoader.loadClass(this.startClassName);
                Method mainMethod = startClass.getMethod("main", String[].class);
                if (!mainMethod.isAccessible()) {
                    mainMethod.setAccessible(true);
                }

                mainMethod.invoke((Object)null, this.args);
            } catch (NoSuchMethodException var5) {
                Exception wrappedEx = new Exception("The specified mainClass doesn't contain a main method with appropriate signature.", var5);
                thread.getThreadGroup().uncaughtException(thread, wrappedEx);
            } catch (Exception var6) {
                thread.getThreadGroup().uncaughtException(thread, var6);
            }

        }
    }


    class IsolatedThreadGroup extends ThreadGroup {
        private final Object monitor;
        private Throwable exception;

        IsolatedThreadGroup(String name) {
            super(name);
            this.monitor = new Object();
        }

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            if (!(ex instanceof ThreadDeath)) {
                synchronized(this.monitor) {
                    this.exception = this.exception != null ? this.exception : ex;
                }

                AbstractRunMojo.this.getLog().warn(ex);
            }

        }

        public void rethrowUncaughtException() throws MojoExecutionException {
            synchronized(this.monitor) {
                if (this.exception != null) {
                    throw new MojoExecutionException("An exception occurred while running. " + this.exception.getMessage(), this.exception);
                }
            }
        }
    }

    private static class TestArtifactFilter extends AbstractArtifactFeatureFilter {
        TestArtifactFilter() {
            super("", "test");
        }
        @Override
        protected String getArtifactFeature(Artifact artifact) {
            return artifact.getScope();
        }
    }
}

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(
    name = "stop",
    requiresProject = true,
    defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST
)
public class StopMojo extends AbstractMojo {
    @Parameter(
        defaultValue = "${project}",
        readonly = true,
        required = true
    )
    private MavenProject project;
    @Parameter(
        property = "spring-boot.stop.fork"
    )
    private Boolean fork;
    @Parameter
    private String jmxName = "org.springframework.boot:type=Admin,name=SpringApplication";
    @Parameter
    private int jmxPort = 9001;
    @Parameter(
        property = "spring-boot.stop.skip",
        defaultValue = "false"
    )
    private boolean skip;

    public StopMojo() {
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.skip) {
            this.getLog().debug("skipping stop as per configuration.");
        } else {
            this.getLog().info("Stopping application...");

            try {
                if (this.isForked()) {
                    this.stopForkedProcess();
                } else {
                    this.stop();
                }
            } catch (IOException var2) {
                this.getLog().debug("Service is not reachable anymore (" + var2.getMessage() + ")");
            }

        }
    }

    private boolean isForked() {
        if (this.fork != null) {
            return this.fork;
        } else {
            String property = this.project.getProperties().getProperty("_spring.boot.fork.enabled");
            return Boolean.valueOf(property);
        }
    }

    private void stopForkedProcess() throws IOException, MojoFailureException, MojoExecutionException {
        JMXConnector connector = SpringApplicationAdminClient.connect(this.jmxPort);
        Throwable var2 = null;

        try {
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            this.doStop(connection);
        } catch (Throwable var11) {
            var2 = var11;
            throw var11;
        } finally {
            if (connector != null) {
                if (var2 != null) {
                    try {
                        connector.close();
                    } catch (Throwable var10) {
                        var2.addSuppressed(var10);
                    }
                } else {
                    connector.close();
                }
            }

        }

    }

    private void stop() throws IOException, MojoFailureException, MojoExecutionException {
        this.doStop(ManagementFactory.getPlatformMBeanServer());
    }

    private void doStop(MBeanServerConnection connection) throws IOException, MojoExecutionException {
        try {
            (new SpringApplicationAdminClient(connection, this.jmxName)).stop();
        } catch (InstanceNotFoundException var3) {
            throw new MojoExecutionException("Spring application lifecycle JMX bean not found (fork is " + this.fork + "). Could not stop application gracefully", var3);
        }
    }
}

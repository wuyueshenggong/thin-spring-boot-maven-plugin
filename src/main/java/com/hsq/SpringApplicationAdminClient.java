//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import java.io.IOException;
import java.util.Map;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.maven.plugin.MojoExecutionException;

class SpringApplicationAdminClient {
    static final String DEFAULT_OBJECT_NAME = "org.springframework.boot:type=Admin,name=SpringApplication";
    private final MBeanServerConnection connection;
    private final ObjectName objectName;

    SpringApplicationAdminClient(MBeanServerConnection connection, String jmxName) {
        this.connection = connection;
        this.objectName = this.toObjectName(jmxName);
    }

    public boolean isReady() throws MojoExecutionException {
        try {
            return (Boolean)this.connection.getAttribute(this.objectName, "Ready");
        } catch (InstanceNotFoundException var2) {
            return false;
        } catch (AttributeNotFoundException var3) {
            throw new IllegalStateException("Unexpected: attribute 'Ready' not available", var3);
        } catch (ReflectionException var4) {
            throw new MojoExecutionException("Failed to retrieve Ready attribute", var4.getCause());
        } catch (IOException | MBeanException var5) {
            throw new MojoExecutionException(var5.getMessage(), var5);
        }
    }

    public void stop() throws MojoExecutionException, IOException, InstanceNotFoundException {
        try {
            this.connection.invoke(this.objectName, "shutdown", (Object[])null, (String[])null);
        } catch (ReflectionException var2) {
            throw new MojoExecutionException("Shutdown failed", var2.getCause());
        } catch (MBeanException var3) {
            throw new MojoExecutionException("Could not invoke shutdown operation", var3);
        }
    }

    private ObjectName toObjectName(String name) {
        try {
            return new ObjectName(name);
        } catch (MalformedObjectNameException var3) {
            throw new IllegalArgumentException("Invalid jmx name '" + name + "'");
        }
    }

    public static JMXConnector connect(int port) throws IOException {
        String url = "service:jmx:rmi:///jndi/rmi://127.0.0.1:" + port + "/jmxrmi";
        JMXServiceURL serviceUrl = new JMXServiceURL(url);
        return JMXConnectorFactory.connect(serviceUrl, (Map)null);
    }
}

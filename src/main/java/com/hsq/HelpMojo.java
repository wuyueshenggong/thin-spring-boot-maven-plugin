//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Mojo(
    name = "help",
    requiresProject = false,
    threadSafe = true
)
public class HelpMojo extends AbstractMojo {
    @Parameter(
        property = "detail",
        defaultValue = "false"
    )
    private boolean detail;
    @Parameter(
        property = "goal"
    )
    private String goal;
    @Parameter(
        property = "lineLength",
        defaultValue = "80"
    )
    private int lineLength;
    @Parameter(
        property = "indentSize",
        defaultValue = "2"
    )
    private int indentSize;
    private static final String PLUGIN_HELP_PATH = "/META-INF/maven/org.springframework.boot/spring-boot-maven-plugin/plugin-help.xml";
    private static final int DEFAULT_LINE_LENGTH = 80;

    public HelpMojo() {
    }

    private Document build() throws MojoExecutionException {
        this.getLog().debug("load plugin-help.xml: /META-INF/maven/org.springframework.boot/spring-boot-maven-plugin/plugin-help.xml");
        InputStream is = null;

        Document var4;
        try {
            is = this.getClass().getResourceAsStream("/META-INF/maven/org.springframework.boot/spring-boot-maven-plugin/plugin-help.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            var4 = dBuilder.parse(is);
        } catch (IOException var15) {
            throw new MojoExecutionException(var15.getMessage(), var15);
        } catch (ParserConfigurationException var16) {
            throw new MojoExecutionException(var16.getMessage(), var16);
        } catch (SAXException var17) {
            throw new MojoExecutionException(var17.getMessage(), var17);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException var14) {
                    throw new MojoExecutionException(var14.getMessage(), var14);
                }
            }

        }

        return var4;
    }

    public void execute() throws MojoExecutionException {
        if (this.lineLength <= 0) {
            this.getLog().warn("The parameter 'lineLength' should be positive, using '80' as default.");
            this.lineLength = 80;
        }

        if (this.indentSize <= 0) {
            this.getLog().warn("The parameter 'indentSize' should be positive, using '2' as default.");
            this.indentSize = 2;
        }

        Document doc = this.build();
        StringBuilder sb = new StringBuilder();
        Node plugin = this.getSingleChild(doc, "plugin");
        String name = this.getValue(plugin, "name");
        String version = this.getValue(plugin, "version");
        String id = this.getValue(plugin, "groupId") + ":" + this.getValue(plugin, "artifactId") + ":" + version;
        if (isNotEmpty(name) && !name.contains(id)) {
            this.append(sb, name + " " + version, 0);
        } else if (isNotEmpty(name)) {
            this.append(sb, name, 0);
        } else {
            this.append(sb, id, 0);
        }

        this.append(sb, this.getValue(plugin, "description"), 1);
        this.append(sb, "", 0);
        String goalPrefix = this.getValue(plugin, "goalPrefix");
        Node mojos1 = this.getSingleChild(plugin, "mojos");
        List<Node> mojos = this.findNamedChild(mojos1, "mojo");
        if (this.goal == null || this.goal.length() <= 0) {
            this.append(sb, "This plugin has " + mojos.size() + (mojos.size() > 1 ? " goals:" : " goal:"), 0);
            this.append(sb, "", 0);
        }

        Iterator var10 = mojos.iterator();

        while(var10.hasNext()) {
            Node mojo = (Node)var10.next();
            this.writeGoal(sb, goalPrefix, (Element)mojo);
        }

        if (this.getLog().isInfoEnabled()) {
            this.getLog().info(sb.toString());
        }

    }

    private static boolean isNotEmpty(String string) {
        return string != null && string.length() > 0;
    }

    private String getValue(Node node, String elementName) throws MojoExecutionException {
        return this.getSingleChild(node, elementName).getTextContent();
    }

    private Node getSingleChild(Node node, String elementName) throws MojoExecutionException {
        List<Node> namedChild = this.findNamedChild(node, elementName);
        if (namedChild.isEmpty()) {
            throw new MojoExecutionException("Could not find " + elementName + " in plugin-help.xml");
        } else if (namedChild.size() > 1) {
            throw new MojoExecutionException("Multiple " + elementName + " in plugin-help.xml");
        } else {
            return (Node)namedChild.get(0);
        }
    }

    private List<Node> findNamedChild(Node node, String elementName) {
        List<Node> result = new ArrayList();
        NodeList childNodes = node.getChildNodes();

        for(int i = 0; i < childNodes.getLength(); ++i) {
            Node item = childNodes.item(i);
            if (elementName.equals(item.getNodeName())) {
                result.add(item);
            }
        }

        return result;
    }

    private Node findSingleChild(Node node, String elementName) throws MojoExecutionException {
        List<Node> elementsByTagName = this.findNamedChild(node, elementName);
        if (elementsByTagName.isEmpty()) {
            return null;
        } else if (elementsByTagName.size() > 1) {
            throw new MojoExecutionException("Multiple " + elementName + "in plugin-help.xml");
        } else {
            return (Node)elementsByTagName.get(0);
        }
    }

    private void writeGoal(StringBuilder sb, String goalPrefix, Element mojo) throws MojoExecutionException {
        String mojoGoal = this.getValue(mojo, "goal");
        Node configurationElement = this.findSingleChild(mojo, "configuration");
        Node description = this.findSingleChild(mojo, "description");
        if (this.goal == null || this.goal.length() <= 0 || mojoGoal.equals(this.goal)) {
            this.append(sb, goalPrefix + ":" + mojoGoal, 0);
            Node deprecated = this.findSingleChild(mojo, "deprecated");
            if (deprecated != null && isNotEmpty(deprecated.getTextContent())) {
                this.append(sb, "Deprecated. " + deprecated.getTextContent(), 1);
                if (this.detail && description != null) {
                    this.append(sb, "", 0);
                    this.append(sb, description.getTextContent(), 1);
                }
            } else if (description != null) {
                this.append(sb, description.getTextContent(), 1);
            }

            this.append(sb, "", 0);
            if (this.detail) {
                Node parametersNode = this.getSingleChild(mojo, "parameters");
                List<Node> parameters = this.findNamedChild(parametersNode, "parameter");
                this.append(sb, "Available parameters:", 1);
                this.append(sb, "", 0);
                Iterator var10 = parameters.iterator();

                while(var10.hasNext()) {
                    Node parameter = (Node)var10.next();
                    this.writeParameter(sb, parameter, configurationElement);
                }
            }
        }

    }

    private void writeParameter(StringBuilder sb, Node parameter, Node configurationElement) throws MojoExecutionException {
        String parameterName = this.getValue(parameter, "name");
        String parameterDescription = this.getValue(parameter, "description");
        Element fieldConfigurationElement = null;
        if (configurationElement != null) {
            fieldConfigurationElement = (Element)this.findSingleChild(configurationElement, parameterName);
        }

        String parameterDefaultValue = "";
        if (fieldConfigurationElement != null && fieldConfigurationElement.hasAttribute("default-value")) {
            parameterDefaultValue = " (Default: " + fieldConfigurationElement.getAttribute("default-value") + ")";
        }

        this.append(sb, parameterName + parameterDefaultValue, 2);
        Node deprecated = this.findSingleChild(parameter, "deprecated");
        if (deprecated != null && isNotEmpty(deprecated.getTextContent())) {
            this.append(sb, "Deprecated. " + deprecated.getTextContent(), 3);
            this.append(sb, "", 0);
        }

        this.append(sb, parameterDescription, 3);
        if ("true".equals(this.getValue(parameter, "required"))) {
            this.append(sb, "Required: Yes", 3);
        }

        if (fieldConfigurationElement != null && isNotEmpty(fieldConfigurationElement.getTextContent())) {
            String property = this.getPropertyFromExpression(fieldConfigurationElement.getTextContent());
            this.append(sb, "User property: " + property, 3);
        }

        this.append(sb, "", 0);
    }

    private static String repeat(String str, int repeat) {
        StringBuilder buffer = new StringBuilder(repeat * str.length());

        for(int i = 0; i < repeat; ++i) {
            buffer.append(str);
        }

        return buffer.toString();
    }

    private void append(StringBuilder sb, String description, int indent) {
        Iterator var4 = toLines(description, indent, this.indentSize, this.lineLength).iterator();

        while(var4.hasNext()) {
            String line = (String)var4.next();
            sb.append(line).append('\n');
        }

    }

    private static List<String> toLines(String text, int indent, int indentSize, int lineLength) {
        List<String> lines = new ArrayList();
        String ind = repeat("\t", indent);
        String[] plainLines = text.split("(\r\n)|(\r)|(\n)");
        String[] var7 = plainLines;
        int var8 = plainLines.length;

        for(int var9 = 0; var9 < var8; ++var9) {
            String plainLine = var7[var9];
            toLines(lines, ind + plainLine, indentSize, lineLength);
        }

        return lines;
    }

    private static void toLines(List<String> lines, String line, int indentSize, int lineLength) {
        int lineIndent = getIndentLevel(line);
        StringBuilder buf = new StringBuilder(256);
        String[] tokens = line.split(" +");
        String[] var7 = tokens;
        int var8 = tokens.length;

        for(int var9 = 0; var9 < var8; ++var9) {
            String token = var7[var9];
            if (buf.length() > 0) {
                if (buf.length() + token.length() >= lineLength) {
                    lines.add(buf.toString());
                    buf.setLength(0);
                    buf.append(repeat(" ", lineIndent * indentSize));
                } else {
                    buf.append(' ');
                }
            }

            for(int j = 0; j < token.length(); ++j) {
                char c = token.charAt(j);
                if (c == '\t') {
                    buf.append(repeat(" ", indentSize - buf.length() % indentSize));
                } else if (c == 160) {
                    buf.append(' ');
                } else {
                    buf.append(c);
                }
            }
        }

        lines.add(buf.toString());
    }

    private static int getIndentLevel(String line) {
        int level = 0;

        int i;
        for(i = 0; i < line.length() && line.charAt(i) == '\t'; ++i) {
            ++level;
        }

        for(i = level + 1; i <= level + 4 && i < line.length(); ++i) {
            if (line.charAt(i) == '\t') {
                ++level;
                break;
            }
        }

        return level;
    }

    private String getPropertyFromExpression(String expression) {
        return expression != null && expression.startsWith("${") && expression.endsWith("}") && !expression.substring(2).contains("${") ? expression.substring(2, expression.length() - 1) : null;
    }
}

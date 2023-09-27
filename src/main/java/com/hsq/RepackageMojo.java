//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.apache.maven.shared.artifact.filter.collection.ArtifactsFilter;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;
import org.apache.maven.shared.artifact.filter.collection.ScopeFilter;
import org.springframework.boot.loader.tools.DefaultLaunchScript;
import org.springframework.boot.loader.tools.LaunchScript;
import org.springframework.boot.loader.tools.Layout;
import org.springframework.boot.loader.tools.LayoutFactory;
import org.springframework.boot.loader.tools.Repackager;
import org.springframework.boot.loader.tools.Layouts.Expanded;
import org.springframework.boot.loader.tools.Layouts.Jar;
import org.springframework.boot.loader.tools.Layouts.None;
import org.springframework.boot.loader.tools.Layouts.War;
import org.springframework.boot.loader.tools.Repackager.MainClassTimeoutWarningListener;

@Mojo(
    name = "repackage",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresProject = true,
    threadSafe = true,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class RepackageMojo extends AbstractDependencyFilterMojo {
    private static final Pattern WHITE_SPACE_PATTERN = Pattern.compile("\\s+");
    @Parameter(
        defaultValue = "${project}",
        readonly = true,
        required = true
    )
    private MavenProject project;
    @Component
    private MavenProjectHelper projectHelper;
    @Parameter(
        defaultValue = "${project.build.directory}",
        required = true
    )
    private File outputDirectory;
    @Parameter(
        defaultValue = "${project.build.finalName}",
        readonly = true
    )
    private String finalName;
    @Parameter(
        property = "spring-boot.repackage.skip",
        defaultValue = "false"
    )
    private boolean skip;

    @Parameter(
            property = "spring-boot.rethin",
            defaultValue = "false"
    )
    private boolean rethin;

    @Parameter(
            property = "spring-boot.windDir",
            defaultValue = "C:/thinMavenPlugin/ea/"
    )
    private String windDir;

    @Parameter(
            property = "spring-boot.unixDir",
            defaultValue = "/var/thinMavenPlugin/ea/"
    )
    private String unixDir;

    @Parameter
    private String classifier;
    @Parameter(
        defaultValue = "true"
    )
    private boolean attach = true;
    @Parameter
    private String mainClass;
    @Parameter
    private RepackageMojo.LayoutType layout;
    @Parameter
    private LayoutFactory layoutFactory;
    @Parameter
    private List<Dependency> requiresUnpack;
    @Parameter(
        defaultValue = "false"
    )
    private boolean executable;
    @Parameter
    private File embeddedLaunchScript;
    @Parameter
    private Properties embeddedLaunchScriptProperties;
    @Parameter(
        defaultValue = "true"
    )
    private boolean excludeDevtools = true;
    @Parameter(
        defaultValue = "false"
    )
    public boolean includeSystemScope;

    public RepackageMojo() {
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.project.getPackaging().equals("pom")) {
            this.getLog().debug("repackage goal could not be applied to pom project.");
        } else if (this.skip) {
            this.getLog().debug("skipping repackaging as per configuration.");
        } else {
            this.repackage();
        }
    }

    private Map<String,Artifact> artifact2Map(Set<Artifact> allArtifacts){
        Map<String,Artifact> id2Artifact = new HashMap<>(allArtifacts.size());
        for(Artifact artifact:allArtifacts){
            String classifier = artifact.getClassifier();
            classifier = classifier == null ?"":classifier;
            id2Artifact.put(artifact.getGroupId()+":"+artifact.getArtifactId()+":"+artifact.getVersion()+":"+classifier,artifact);
        }
        return id2Artifact;
    }



    /**
     * 除了配置以外的jar都不会打入到最终的jar中，
     * 其他的包因为不会更新太频繁，它们将会以loader.path的方式进行加载，从而将jar包进行瘦身。
     * 一旦项目引入了新的jar包，那么也不会打入进去
     * 为了解决这个问题，引入排除文件。
     * 如果被排除的文件不在上一次排除的范围内（视为新增加的jar），则将其打入到包中。
     * 直到累计到一定的数量时，可再次瘦身。
     * @return 获取被排除的文件
     */
    private File getLastTimeExcludedArtifactsFile(){
        String projectName = project.getName();
        MavenProject pp = project;
        do{
            pp = pp.getParent();
            if(pp != null){
                projectName = pp.getName();
            }
        }while (pp != null);
	    String tailPath = projectName+"/"+project.getGroupId()+"/"+project.getArtifactId()+"/"+project.getVersion()+"/excludeArtifacts.txt";
        boolean windows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        String root = windows?windDir:unixDir;
        root = root.endsWith("/")?root:root+"/";
        String path = root+tailPath;
        return new File(path);
    }

    /**
     * 如果排除的文件不存在 写入文件
     * 如果文件存在，比较被排除的依赖
     *
     * @param id2Artifact
     * @param artifacts
     */
    private void additionalOperation(Map<String,Artifact> id2Artifact,Set<Artifact> artifacts){
        File excludeFile = getLastTimeExcludedArtifactsFile();
        Map<String,Artifact> restMap = artifact2Map(artifacts);
        //被排除的jar
        Set<String> filtered = new HashSet<>();
        for (String s : id2Artifact.keySet()) {
            if(!restMap.containsKey(s)){
                filtered.add(s);
            }
        }
        if(filtered.size() > 0){
            if(rethin || !excludeFile.exists()){
                //是否重新瘦身
                if(excludeFile.exists()){
                    excludeFile.delete();
                }
                BufferedWriter fw = null;
                try {
	                File directory = excludeFile.getParentFile();
	                boolean de = true;
	                if(!directory.exists()){
	                	de = directory.mkdirs();
	                }
                    if(de && excludeFile.createNewFile()){
                        //写入被排除的jar包
                        fw = new BufferedWriter(new FileWriter(excludeFile));
                        fw.write("打包辅助文件，请不要修改，不要删除！");
                        fw.newLine();
                        for (String s : filtered) {
                            fw.write(s);
                            fw.newLine();
                        }
                        fw.flush();
                        fw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    getLog().error("thin-maven-plugin repackage写入排除文件失败",e);
                }finally {
                    if(fw != null){
                        try {
                            fw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }else if(excludeFile.exists()){
                List<String> lastTimeExcludedArtifacts = new ArrayList<>();
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(excludeFile));
                    lastTimeExcludedArtifacts = br.lines().collect(Collectors.toList());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    getLog().error("thin-maven-plugin repackage读取排除文件失败",e);
                } finally {
                    if(br != null){
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Map<String,Artifact> newUnpackedMap = new HashMap<>();
                for (String s : filtered) {
                    if(!lastTimeExcludedArtifacts.contains(s)){
                        //新增的
                        newUnpackedMap.put(s,id2Artifact.get(s));
                    }
                }
                List<Exclude> excludes = getExcludes();
                if(newUnpackedMap.size() > 0 ){
                    Set<Artifact> newUnpackedValues = new HashSet<>(newUnpackedMap.values());
                    if(excludes != null && excludes.size() > 0){
                        //排除是否有可能是exclude新增的
                        FilterArtifacts filters = new FilterArtifacts();
                        filters.addFilter(new ExcludeFilter(excludes));
                        try {
                            newUnpackedValues = filters.filter(newUnpackedValues);
                        } catch (ArtifactFilterException e) {
                            e.printStackTrace();
                        }
                    }
                    if(newUnpackedValues.size() > 0){
                        //发现增加
                        artifacts.addAll(newUnpackedValues);
                    }
                }
            }
        }
    }

    private void repackage() throws MojoExecutionException {
        Artifact source = this.getSourceArtifact();
        File target = this.getTargetFile();
        Repackager repackager = this.getRepackager(source.getFile());
        Map<String,Artifact> id2Artifact = artifact2Map(this.project.getArtifacts());
        Set<Artifact> artifacts = this.filterDependencies(this.project.getArtifacts(), this.getFilters(this.getAdditionalFilters()));
        additionalOperation(id2Artifact,artifacts);
        ArtifactsLibraries libraries = new ArtifactsLibraries(artifacts, this.requiresUnpack, this.getLog());
        try {
            LaunchScript launchScript = this.getLaunchScript();
            repackager.repackage(target, libraries, launchScript);
        } catch (IOException var7) {
            throw new MojoExecutionException(var7.getMessage(), var7);
        }

        this.updateArtifact(source, target, repackager.getBackupFile());
    }

    private Artifact getSourceArtifact() {
        Artifact sourceArtifact = this.getArtifact(this.classifier);
        return sourceArtifact != null ? sourceArtifact : this.project.getArtifact();
    }

    private Artifact getArtifact(String classifier) {
        if (classifier != null) {
            Iterator var2 = this.project.getAttachedArtifacts().iterator();

            while(var2.hasNext()) {
                Artifact attachedArtifact = (Artifact)var2.next();
                if (classifier.equals(attachedArtifact.getClassifier()) && attachedArtifact.getFile() != null && attachedArtifact.getFile().isFile()) {
                    return attachedArtifact;
                }
            }
        }

        return null;
    }

    private File getTargetFile() {
        String classifier = this.classifier != null ? this.classifier.trim() : "";
        if (!classifier.isEmpty() && !classifier.startsWith("-")) {
            classifier = "-" + classifier;
        }

        if (!this.outputDirectory.exists()) {
            this.outputDirectory.mkdirs();
        }

        return new File(this.outputDirectory, this.finalName + classifier + "." + this.project.getArtifact().getArtifactHandler().getExtension());
    }

    private Repackager getRepackager(File source) {
        Repackager repackager = new Repackager(source, this.layoutFactory);
        repackager.addMainClassTimeoutWarningListener(new RepackageMojo.LoggingMainClassTimeoutWarningListener());
        repackager.setMainClass(this.mainClass);
        if (this.layout != null) {
            this.getLog().info("Layout: " + this.layout);
            repackager.setLayout(this.layout.layout());
        }

        return repackager;
    }

    private ArtifactsFilter[] getAdditionalFilters() {
        List<ArtifactsFilter> filters = new ArrayList();
        if (this.excludeDevtools) {
            Exclude exclude = new Exclude();
            exclude.setGroupId("org.springframework.boot");
            exclude.setArtifactId("spring-boot-devtools");
            ExcludeFilter filter = new ExcludeFilter(new Exclude[]{exclude});
            filters.add(filter);
        }

        if (!this.includeSystemScope) {
            filters.add(new ScopeFilter((String)null, "system"));
        }

        return (ArtifactsFilter[])filters.toArray(new ArtifactsFilter[0]);
    }

    private LaunchScript getLaunchScript() throws IOException {
        return !this.executable && this.embeddedLaunchScript == null ? null : new DefaultLaunchScript(this.embeddedLaunchScript, this.buildLaunchScriptProperties());
    }

    private Properties buildLaunchScriptProperties() {
        Properties properties = new Properties();
        if (this.embeddedLaunchScriptProperties != null) {
            properties.putAll(this.embeddedLaunchScriptProperties);
        }

        this.putIfMissing(properties, "initInfoProvides", this.project.getArtifactId());
        this.putIfMissing(properties, "initInfoShortDescription", this.project.getName(), this.project.getArtifactId());
        this.putIfMissing(properties, "initInfoDescription", this.removeLineBreaks(this.project.getDescription()), this.project.getName(), this.project.getArtifactId());
        return properties;
    }

    private String removeLineBreaks(String description) {
        return description != null ? WHITE_SPACE_PATTERN.matcher(description).replaceAll(" ") : null;
    }

    private void putIfMissing(Properties properties, String key, String... valueCandidates) {
        if (!properties.containsKey(key)) {
            String[] var4 = valueCandidates;
            int var5 = valueCandidates.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String candidate = var4[var6];
                if (candidate != null && !candidate.isEmpty()) {
                    properties.put(key, candidate);
                    return;
                }
            }
        }

    }

    private void updateArtifact(Artifact source, File target, File original) {
        if (this.attach) {
            this.attachArtifact(source, target);
        } else if (source.getFile().equals(target) && original.exists()) {
            String artifactId = this.classifier != null ? "artifact with classifier " + this.classifier : "main artifact";
            this.getLog().info(String.format("Updating %s %s to %s", artifactId, source.getFile(), original));
            source.setFile(original);
        } else if (this.classifier != null) {
            this.getLog().info("Creating repackaged archive " + target + " with classifier " + this.classifier);
        }

    }

    private void attachArtifact(Artifact source, File target) {
        if (this.classifier != null && !source.getFile().equals(target)) {
            this.getLog().info("Attaching repackaged archive " + target + " with classifier " + this.classifier);
            this.projectHelper.attachArtifact(this.project, this.project.getPackaging(), this.classifier, target);
        } else {
            String artifactId = this.classifier != null ? "artifact with classifier " + this.classifier : "main artifact";
            this.getLog().info("Replacing " + artifactId + " with repackaged archive");
            source.setFile(target);
        }

    }

    public static enum LayoutType {
        JAR(new Jar()),
        WAR(new War()),
        ZIP(new Expanded()),
        DIR(new Expanded()),
        NONE(new None());

        private final Layout layout;

        private LayoutType(Layout layout) {
            this.layout = layout;
        }

        public Layout layout() {
            return this.layout;
        }
    }

    private class LoggingMainClassTimeoutWarningListener implements MainClassTimeoutWarningListener {
        private LoggingMainClassTimeoutWarningListener() {
        }

        public void handleTimeoutWarning(long duration, String mainMethod) {
            RepackageMojo.this.getLog().warn("Searching for the main-class is taking some time, consider using the mainClass configuration parameter");
        }
    }
}

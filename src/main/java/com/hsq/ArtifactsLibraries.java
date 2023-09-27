//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.springframework.boot.loader.tools.Libraries;
import org.springframework.boot.loader.tools.Library;
import org.springframework.boot.loader.tools.LibraryCallback;
import org.springframework.boot.loader.tools.LibraryScope;

public class ArtifactsLibraries implements Libraries {
    private static final Map<String, LibraryScope> SCOPES;
    private final Set<Artifact> artifacts;
    private final Collection<Dependency> unpacks;
    private final Log log;

    public ArtifactsLibraries(Set<Artifact> artifacts, Collection<Dependency> unpacks, Log log) {
        this.artifacts = artifacts;
        this.unpacks = unpacks;
        this.log = log;
    }

    @Override
    public void doWithLibraries(LibraryCallback callback) throws IOException {
        Set<String> duplicates = this.getDuplicates(this.artifacts);
        Iterator var3 = this.artifacts.iterator();

        while(var3.hasNext()) {
            Artifact artifact = (Artifact)var3.next();
            LibraryScope scope = (LibraryScope)SCOPES.get(artifact.getScope());
            if (scope != null && artifact.getFile() != null) {
                String name = this.getFileName(artifact);
                if (duplicates.contains(name)) {
                    this.log.debug("Duplicate found: " + name);
                    name = artifact.getGroupId() + "-" + name;
                    this.log.debug("Renamed to: " + name);
                }

                callback.library(new Library(name, artifact.getFile(), scope, this.isUnpackRequired(artifact)));
            }
        }

    }

    private Set<String> getDuplicates(Set<Artifact> artifacts) {
        Set<String> duplicates = new HashSet();
        Set<String> seen = new HashSet();
        Iterator var4 = artifacts.iterator();

        while(var4.hasNext()) {
            Artifact artifact = (Artifact)var4.next();
            String fileName = this.getFileName(artifact);
            if (artifact.getFile() != null && !seen.add(fileName)) {
                duplicates.add(fileName);
            }
        }

        return duplicates;
    }

    private boolean isUnpackRequired(Artifact artifact) {
        if (this.unpacks != null) {
            Iterator var2 = this.unpacks.iterator();

            while(var2.hasNext()) {
                Dependency unpack = (Dependency)var2.next();
                if (artifact.getGroupId().equals(unpack.getGroupId()) && artifact.getArtifactId().equals(unpack.getArtifactId())) {
                    return true;
                }
            }
        }

        return false;
    }

    private String getFileName(Artifact artifact) {
        StringBuilder sb = new StringBuilder();
        sb.append(artifact.getArtifactId()).append("-").append(artifact.getBaseVersion());
        String classifier = artifact.getClassifier();
        if (classifier != null) {
            sb.append("-").append(classifier);
        }

        sb.append(".").append(artifact.getArtifactHandler().getExtension());
        return sb.toString();
    }

    static {
        Map<String, LibraryScope> libraryScopes = new HashMap();
        libraryScopes.put("compile", LibraryScope.COMPILE);
        libraryScopes.put("runtime", LibraryScope.RUNTIME);
        libraryScopes.put("provided", LibraryScope.PROVIDED);
        libraryScopes.put("system", LibraryScope.PROVIDED);
        SCOPES = Collections.unmodifiableMap(libraryScopes);
    }
}

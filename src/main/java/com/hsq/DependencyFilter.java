//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.artifact.filter.collection.AbstractArtifactsFilter;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;

public abstract class DependencyFilter extends AbstractArtifactsFilter {
    private final List<? extends FilterableDependency> filters;

    public DependencyFilter(List<? extends FilterableDependency> dependencies) {
        this.filters = dependencies;
    }
    @Override
    public Set filter(Set artifacts) throws ArtifactFilterException {
        Set result = new HashSet();
        Iterator var3 = artifacts.iterator();

        while(var3.hasNext()) {
            Object artifact = var3.next();
            if (!this.filter((Artifact)artifact)) {
                result.add(artifact);
            }
        }

        return result;
    }

    protected abstract boolean filter(Artifact artifact);

    protected final boolean equals(Artifact artifact, FilterableDependency dependency) {
        //开启模式匹配
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();
        Pattern p1 = Pattern.compile(groupId);
        Matcher matcher = p1.matcher(artifact.getGroupId());
        boolean match = false;
        if(matcher.matches()){
            p1 = Pattern.compile(artifactId);
            matcher = p1.matcher(artifact.getArtifactId());
            match =matcher.matches();
        }
        if(match){
            if(dependency.getClassifier() != null){
                p1 = Pattern.compile(dependency.getClassifier());
                matcher = p1.matcher(artifact.getClassifier());
                return matcher.matches();
            }
            return true;
        }
        return false;
    }

    protected final List<? extends FilterableDependency> getFilters() {
        return this.filters;
    }
}

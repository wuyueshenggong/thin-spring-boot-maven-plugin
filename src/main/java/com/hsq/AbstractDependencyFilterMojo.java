//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.apache.maven.shared.artifact.filter.collection.ArtifactsFilter;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;

public abstract class AbstractDependencyFilterMojo extends AbstractMojo {
    @Parameter(
        property = "spring-boot.includes"
    )
    private List<Include> includes;
    @Parameter(
        property = "spring-boot.excludes"
    )
    private List<Exclude> excludes;
    @Parameter(
        property = "spring-boot.excludeGroupIds",
        defaultValue = ""
    )
    private String excludeGroupIds;

    public AbstractDependencyFilterMojo() {
    }

    protected void setExcludes(List<Exclude> excludes) {
        this.excludes = excludes;
    }

    protected void setIncludes(List<Include> includes) {
        this.includes = includes;
    }

    protected void setExcludeGroupIds(String excludeGroupIds) {
        this.excludeGroupIds = excludeGroupIds;
    }

    protected Set<Artifact> filterDependencies(Set<Artifact> dependencies, FilterArtifacts filters) throws MojoExecutionException {
        try {
            Set<Artifact> filtered = new LinkedHashSet(dependencies);
            filtered.retainAll(filters.filter(dependencies));
            return filtered;
        } catch (ArtifactFilterException var4) {
            throw new MojoExecutionException(var4.getMessage(), var4);
        }
    }

    protected final FilterArtifacts getFilters(ArtifactsFilter... additionalFilters) {
        FilterArtifacts filters = new FilterArtifacts();
        ArtifactsFilter[] var3 = additionalFilters;
        int var4 = additionalFilters.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            ArtifactsFilter additionalFilter = var3[var5];
            filters.addFilter(additionalFilter);
        }

        filters.addFilter(new MatchingGroupIdFilter(this.cleanFilterConfig(this.excludeGroupIds)));
        if (this.includes != null && !this.includes.isEmpty()) {
            filters.addFilter(new IncludeFilter(this.includes));
        }

        if (this.excludes != null && !this.excludes.isEmpty()) {
            filters.addFilter(new ExcludeFilter(this.excludes));
        }

        return filters;
    }

    private String cleanFilterConfig(String content) {
        if (content != null && !content.trim().isEmpty()) {
            StringBuilder cleaned = new StringBuilder();
            StringTokenizer tokenizer = new StringTokenizer(content, ",");

            while(tokenizer.hasMoreElements()) {
                cleaned.append(tokenizer.nextToken().trim());
                if (tokenizer.hasMoreElements()) {
                    cleaned.append(",");
                }
            }

            return cleaned.toString();
        } else {
            return "";
        }
    }

    public List<Exclude> getExcludes() {
        return excludes;
    }
}

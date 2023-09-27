//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.artifact.Artifact;

public class ExcludeFilter extends DependencyFilter {
    public ExcludeFilter(Exclude... excludes) {
        this(Arrays.asList(excludes));
    }

    public ExcludeFilter(List<Exclude> excludes) {
        super(excludes);
    }
    @Override
    protected boolean filter(Artifact artifact) {
        Iterator var2 = this.getFilters().iterator();

        FilterableDependency dependency;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            dependency = (FilterableDependency)var2.next();
        } while(!this.equals(artifact, dependency));

        return true;
    }
}

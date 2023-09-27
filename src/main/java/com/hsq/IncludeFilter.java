//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import java.util.Iterator;
import java.util.List;
import org.apache.maven.artifact.Artifact;

public class IncludeFilter extends DependencyFilter {
    public IncludeFilter(List<Include> includes) {
        super(includes);
    }
    @Override
    protected boolean filter(Artifact artifact) {
        Iterator var2 = this.getFilters().iterator();

        FilterableDependency dependency;
        do {
            if (!var2.hasNext()) {
                return true;
            }

            dependency = (FilterableDependency)var2.next();
        } while(!this.equals(artifact, dependency));

        return false;
    }
}

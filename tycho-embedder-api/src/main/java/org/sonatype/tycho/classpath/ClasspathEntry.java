package org.sonatype.tycho.classpath;

import java.io.File;
import java.util.List;

import org.sonatype.tycho.ArtifactKey;
import org.sonatype.tycho.ReactorProject;

/**
 * @author igor
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ClasspathEntry
{

    /**
     * @noimplement This interface is not intended to be implemented by clients.
     */
    public static interface AccessRule
    {
        public String getPattern();

        public boolean isDiscouraged();
    }

    /** 
     * ArtifactKey that corresponds to this classpath entry. Not null.
     */
    public ArtifactKey getArtifactKey();

    /**
     * MavenProject that corresponds to this classpath entry or null, if no such project.
     */
    public ReactorProject getMavenProject();

    /**
     * Jar files and/or class folders that correspond to this classpath entry. Projects and bundles with nested jars
     * can have multiple locations.
     */
    public List<File> getLocations();

    /**
     * Exported packages patterns. Empty list means "no exported packages". <code>null</code> means
     * "no access restrictions", i.e. all packages are exported.
     */
    public List<AccessRule> getAccessRules();

}

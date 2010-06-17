package org.sonatype.tycho.p2.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.p2.director.Explanation;
import org.eclipse.equinox.internal.p2.director.Projector;
import org.eclipse.equinox.internal.p2.director.QueryableArray;
import org.eclipse.equinox.internal.p2.director.SimplePlanner;
import org.eclipse.equinox.internal.p2.director.Slicer;
import org.eclipse.equinox.internal.p2.metadata.IRequiredCapability;
import org.eclipse.equinox.internal.p2.metadata.InstallableUnit;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IProvidedCapability;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.metadata.MetadataFactory;
import org.eclipse.equinox.p2.metadata.MetadataFactory.InstallableUnitDescription;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.equinox.p2.metadata.expression.IMatchExpression;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;

@SuppressWarnings( "restriction" )
public class ProjectorResolutionStrategy
    extends ResolutionStrategy
{
    private static final IInstallableUnit[] IU_ARRAY = new IInstallableUnit[0];

    private final Map<String, String> properties;

    public ProjectorResolutionStrategy( Map<String, String> properties )
    {
        this.properties = properties;
    }

    public Collection<IInstallableUnit> resolve( IProgressMonitor monitor )
    {
        Map<String, String> newSelectionContext = SimplePlanner.createSelectionContext( properties );

        // additional requirements can be either ius or bundles, and apparently projector does not like bundle
        // requirements to be listed directly under entryPointIU (i.e. the first Project.encode parameter)
        // To workaround this, we do the following
        // 1. wrap additional requirements into extra IU
        // 2. add the extra IU as requirement of entryPointIU
        // 3. tell projector the extra IUs is already installed, via Project.encode alreadyExistingRoots parameter

        Set<IInstallableUnit> extraIUs = createAdditionalRequirementsIU();

        Set<IInstallableUnit> rootWithExtraIUs = new LinkedHashSet<IInstallableUnit>();
        rootWithExtraIUs.addAll( rootIUs );
        rootWithExtraIUs.addAll( extraIUs );

        Slicer slicer = new Slicer( availableIUs, newSelectionContext, false );
        IQueryable<IInstallableUnit> slice = slicer.slice( rootWithExtraIUs.toArray( IU_ARRAY ), monitor );

        if ( slice == null )
        {
            throw new RuntimeException( new CoreException( slicer.getStatus() ) );
        }

        Projector projector = new Projector( slice, newSelectionContext, new HashSet<IInstallableUnit>(), false );
        projector.encode( createMetaIU( rootIUs ), extraIUs.toArray( IU_ARRAY ) /* alreadyExistingRoots */,
                          new QueryableArray( IU_ARRAY ) /* installed IUs */, rootIUs /* newRoots */, monitor );
        IStatus s = projector.invokeSolver( monitor );
        if ( s.getSeverity() == IStatus.ERROR )
        {
            Set<Explanation> explanation = projector.getExplanation( monitor );

            System.out.println( newSelectionContext.toString() );
            System.out.println( explanation );

            throw new RuntimeException( new ProvisionException( s ) );
        }
        Collection<IInstallableUnit> newState = projector.extractSolution();

        fixSWT( newState, newSelectionContext, monitor );

        return newState;
    }

    private void fixSWT( Collection<IInstallableUnit> ius, Map<String, String> newSelectionContext,
                         IProgressMonitor monitor )
    {
        boolean swt = false;
        for ( IInstallableUnit iu : ius )
        {
            if ( "org.eclipse.swt".equals( iu.getId() ) )
            {
                swt = true;
                break;
            }
        }

        if ( !swt )
        {
            return;
        }

        IInstallableUnit swtFragment = null;

        all_ius: for ( Iterator<IInstallableUnit> iter = availableIUs.query( QueryUtil.ALL_UNITS, monitor ).iterator(); iter.hasNext(); )
        {
            IInstallableUnit iu = iter.next();
            if ( iu.getId().startsWith( "org.eclipse.swt" ) && isApplicable( newSelectionContext, iu.getFilter() ) )
            {
                for ( IProvidedCapability provided : iu.getProvidedCapabilities() )
                {
                    if ( "osgi.fragment".equals( provided.getNamespace() )
                        && "org.eclipse.swt".equals( provided.getName() ) )
                    {
                        if ( swtFragment == null || swtFragment.getVersion().compareTo( iu.getVersion() ) < 0 )
                        {
                            swtFragment = iu;
                        }
                        continue all_ius;
                    }
                }
            }
        }

        if ( swtFragment == null )
        {
            throw new RuntimeException( "Could not determine SWT implementation fragment bundle" );
        }

        ius.add( swtFragment );
    }

    protected boolean isApplicable( Map<String, String> selectionContext, IMatchExpression<IInstallableUnit> filter )
    {
        if ( filter == null )
        {
            return true;
        }

        return filter.isMatch( InstallableUnit.contextIU( selectionContext ) );
    }

    private IInstallableUnit createMetaIU( Set<IInstallableUnit> rootIUs )
    {
        InstallableUnitDescription iud = new MetadataFactory.InstallableUnitDescription();
        String time = Long.toString( System.currentTimeMillis() );
        iud.setId( time );
        iud.setVersion( Version.createOSGi( 0, 0, 0, time ) );

        ArrayList<IRequirement> requirements = new ArrayList<IRequirement>();
        for ( IInstallableUnit iu : rootIUs )
        {
            VersionRange range = new VersionRange( iu.getVersion(), true, iu.getVersion(), true );
            requirements.add( MetadataFactory.createRequirement( IInstallableUnit.NAMESPACE_IU_ID, iu.getId(), range,
                                                                 iu.getFilter(), 1 /* min */, iu.isSingleton() ? 1
                                                                                 : Integer.MAX_VALUE /* max */, true /* greedy */) );
        }

        requirements.addAll( additionalRequirements );

        iud.setRequirements( (IRequirement[]) requirements.toArray( new IRequirement[requirements.size()] ) );
        return MetadataFactory.createInstallableUnit( iud );
    }

    private Set<IInstallableUnit> createAdditionalRequirementsIU()
    {
        LinkedHashSet<IInstallableUnit> result = new LinkedHashSet<IInstallableUnit>();

        if ( !additionalRequirements.isEmpty() )
        {
            InstallableUnitDescription iud = new MetadataFactory.InstallableUnitDescription();
            String time = Long.toString( System.currentTimeMillis() );
            iud.setId( "extra-" + time );
            iud.setVersion( Version.createOSGi( 0, 0, 0, time ) );
            iud.setRequirements( additionalRequirements.toArray( new IRequiredCapability[additionalRequirements.size()] ) );

            result.add( MetadataFactory.createInstallableUnit( iud ) );
        }

        return result;
    }

}

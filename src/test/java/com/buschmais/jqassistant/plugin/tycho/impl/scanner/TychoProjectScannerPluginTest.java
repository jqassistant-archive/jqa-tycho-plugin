package com.buschmais.jqassistant.plugin.tycho.impl.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDirectoryDescriptor;

import org.apache.maven.project.MavenProject;
import org.eclipse.tycho.core.TychoConstants;
import org.eclipse.tycho.core.osgitools.project.EclipsePluginProject;
import org.eclipse.tycho.core.shared.BuildProperties;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TychoProjectScannerPluginTest {

    private final static Class<?> clazz = TychoProjectScannerPluginTest.class;

    private Scanner scanner;
    private ScannerContext scannerContext;
    private MavenProject project;
    private Store store;
    private Matcher<? super Collection<? extends File>> matcher;

    public static List<Object[]> data() {
        Object[] nothingSelected = new Object[] { new ArrayList<String>(), new ArrayList<String>(), is(empty()) };
        Object[] oneFileSelected = new Object[] { Collections.singletonList("log"), new ArrayList<String>(),
                hasItems(new File(clazz.getResource("log").getFile())) };
        Object[] oneFileAndFolderSelected = new Object[] { Arrays.asList(new String[] { "log", "cache" }), new ArrayList<String>(),
                hasItems(new File(clazz.getResource("log").getFile())) };
        Object[] oneFileExcluded = new Object[] { Collections.singletonList("log"), Collections.singletonList("log"), is(empty()) };

        return Arrays.asList(new Object[][] { nothingSelected, oneFileSelected, oneFileAndFolderSelected, oneFileExcluded });
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testGetAdditionalFiles(List<String> includes, List<String> excludes,
                                       Matcher<? super Collection<? extends File>> matcher)
        throws Exception {
        this.scanner = mock(Scanner.class);
        this.scannerContext = mock(ScannerContext.class);
        this.store = mock(Store.class);
        this.project = mock(MavenProject.class);
        this.matcher = matcher;

        when(scanner.getContext()).thenReturn(scannerContext);

        EclipsePluginProject pdeProject = mock(EclipsePluginProject.class);
        BuildProperties properties = mock(BuildProperties.class);
        when(properties.getBinExcludes()).thenReturn(excludes);
        when(properties.getBinIncludes()).thenReturn(includes);

        when(project.getContextValue(TychoConstants.CTX_ECLIPSE_PLUGIN_PROJECT)).thenReturn(pdeProject);
        when(pdeProject.getBuildProperties()).thenReturn(properties);
        when(project.getBasedir()).thenReturn(new File(getClass().getResource(".").getFile()));

        JavaClassesDirectoryDescriptor artifactDescriptor = mock(JavaClassesDirectoryDescriptor.class);
        when(artifactDescriptor.getType()).thenReturn("eclipse-plugin");
        List<ArtifactFileDescriptor> artifacts = new ArrayList<>();
        artifacts.add(artifactDescriptor);
        MavenProjectDirectoryDescriptor mavenProjectDirectoryDescriptor = mock(MavenProjectDirectoryDescriptor.class);
        when(mavenProjectDirectoryDescriptor.getCreatesArtifacts()).thenReturn(artifacts);
        when(scannerContext.peek(MavenProjectDirectoryDescriptor.class)).thenReturn(mavenProjectDirectoryDescriptor);

        //----

        TychoProjectScannerPlugin plugin = new TychoProjectScannerPlugin();
        plugin.configure(scannerContext, Collections.<String, Object>emptyMap());
        plugin.scan(project, null, null, scanner);
        // FIXME: add assertions
    }
}

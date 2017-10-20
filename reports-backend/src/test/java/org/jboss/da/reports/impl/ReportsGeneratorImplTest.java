package org.jboss.da.reports.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.cartographer.api.CartographerConnector;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.ProductVersionService;
import org.jboss.da.listings.api.service.WhiteArtifactService;
import org.jboss.da.listings.model.ProductSupportStatus;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.products.api.Artifact;
import org.jboss.da.products.api.Product;
import org.jboss.da.products.api.ProductArtifacts;
import org.jboss.da.products.impl.AggregatedProductProvider;
import org.jboss.da.reports.api.ArtifactReport;
import org.jboss.da.reports.api.VersionLookupResult;
import org.jboss.da.reports.backend.api.DependencyTreeGenerator;
import org.jboss.da.reports.backend.api.VersionFinder;
import org.jboss.da.reports.backend.impl.DependencyTreeGeneratorImpl;
import org.jboss.da.reports.model.rest.GAVRequest;
import org.jboss.da.reports.model.rest.LookupGAVsRequest;
import org.jboss.da.reports.model.rest.LookupReport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RunWith(MockitoJUnitRunner.class)
public class ReportsGeneratorImplTest {

    @Mock
    private AproxConnector aproxClient;

    @Mock
    private CartographerConnector cartographerClient;

    @Mock
    private VersionFinder versionFinderImpl;

    @Mock
    private BlackArtifactService blackArtifactService;

    @Mock
    private WhiteArtifactService whiteArtifactService;

    @Mock
    private ProductVersionService productVersionService;

    @Mock
    private AggregatedProductProvider productProvider;

    @InjectMocks
    @Spy
    private final DependencyTreeGenerator dependencyTreeGenerator = new DependencyTreeGeneratorImpl();

    @InjectMocks
    private ReportsGeneratorImpl generator;

    private final GAV daGAV = new GAV("org.jboss", "dependency-analysis", "1.0.1");

    private final String version = "0.1.0";

    private final GAV daCoreGAV = new GAV("org.jboss.da", "core", version);

    private final List<String> daCoreVersionsNoBest = Arrays.asList("1.1.1.redhat-2",
            "1.2.3.redhat-1", "1.3.4.redhat-3", "1.3.5.redhat-1");

    private final String bestMatchVersion = version + ".redhat-1";

    private final List<String> daCoreVersionsBest = Arrays.asList("1.1.1.redhat-2",
            "1.2.3.redhat-1", "1.3.4.redhat-3", "1.3.5.redhat-1", bestMatchVersion);

    private final GAVDependencyTree daCoreNoDT = new GAVDependencyTree(daCoreGAV, new HashSet<>());

    private final GAV daUtilGAV = new GAV("org.jboss.da", "util", version);

    private final GAV daCommonGAV = new GAV("org.jboss.da", "common", version);

    private final GAVDependencyTree daUtilDT = new GAVDependencyTree(daUtilGAV, new HashSet<>());

    private final GAVDependencyTree daCommonDT = new GAVDependencyTree(daCommonGAV, new HashSet<>());

    private final GAVDependencyTree daCoreDT = new GAVDependencyTree(daCoreGAV, new HashSet<>(
            Arrays.asList(daUtilDT, daCommonDT)));

    private final Product productEAP = new Product("EAP", "7.0", ProductSupportStatus.UNKNOWN);

    private void prepareProductProvider(List<String> versions, List<Product> whitelisted, GAV gav){
        final Set<Artifact> artifacts = versions.stream()
                .map(v -> new Artifact(new GAV(gav.getGA(),v)))
                .collect(Collectors.toSet());

        Set<ProductArtifacts> prodArts = new HashSet<>();
        prodArts.add(new ProductArtifacts(Product.UNKNOWN, artifacts));
        for(Product w : whitelisted){
            prodArts.add(new ProductArtifacts(w, artifacts));
        }

        when(productProvider.getArtifacts(gav.getGA()))
                .thenReturn(CompletableFuture.completedFuture(prodArts));
    }

    private void prepare(List<Product> whitelisted, boolean blacklisted, List<String> versions,
            String best, GAVDependencyTree dependencyTree) throws CommunicationException,
            FindGAVDependencyException {
        when(versionFinderImpl.getBestMatchVersionFor(eq(daCoreGAV), any(List.class))).thenReturn(
                Optional.ofNullable(best));
        when(versionFinderImpl.getVersionsFor(eq(daCoreGAV), any())).thenReturn(
                CompletableFuture.completedFuture(new VersionLookupResult(
                        Optional.ofNullable(best), versions)));

        prepareProductProvider(versions, whitelisted, daCoreGAV);
        when(blackArtifactService.isArtifactPresent(daCoreGAV)).thenReturn(blacklisted);
        when(cartographerClient.getDependencyTreeOfGAV(daCoreGAV)).thenReturn(dependencyTree);
    }

    private void prepareMulti() throws CommunicationException, FindGAVDependencyException {
        prepare(Collections.emptyList(), false, daCoreVersionsBest, bestMatchVersion, daCoreNoDT);
        when(cartographerClient.getDependencyTreeOfGAV(daCoreGAV)).thenReturn(daCoreDT);

        when(versionFinderImpl.getBestMatchVersionFor(eq(daUtilGAV), any(List.class))).thenReturn(
                Optional.ofNullable(bestMatchVersion));
        when(versionFinderImpl.getVersionsFor(eq(daUtilGAV), any())).thenReturn(
                CompletableFuture.completedFuture(new VersionLookupResult(Optional
                        .ofNullable(bestMatchVersion), daCoreVersionsBest)));
        prepareProductProvider(daCoreVersionsBest, Collections.emptyList(), daUtilGAV);

        when(blackArtifactService.isArtifactPresent(daUtilGAV)).thenReturn(false);

        when(versionFinderImpl.getBestMatchVersionFor(eq(daCommonGAV), any(List.class)))
                .thenReturn(Optional.empty());
        when(versionFinderImpl.getVersionsFor(eq(daCommonGAV), any())).thenReturn(
                CompletableFuture.completedFuture(new VersionLookupResult(Optional.empty(),
                        daCoreVersionsNoBest)));
        prepareProductProvider(daCoreVersionsNoBest, Collections.emptyList(), daCommonGAV);
        when(blackArtifactService.isArtifactPresent(daCommonGAV)).thenReturn(false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullGAV() throws CommunicationException, FindGAVDependencyException {
        generator.getReport(null);
    }

    @Test(expected = FindGAVDependencyException.class)
    public void testNonExistingGAV() throws CommunicationException, FindGAVDependencyException {
        when(cartographerClient.getDependencyTreeOfGAV(daGAV)).thenThrow(
                FindGAVDependencyException.class);

        generator.getReport(gavToRequest(daGAV));
    }

    @Test
    public void testNonListedNoBestMatchGAV() throws CommunicationException,
            FindGAVDependencyException {
        prepare(Collections.emptyList(), false, daCoreVersionsNoBest, null, daCoreNoDT);

        ArtifactReport report = generator.getReport(gavToRequest(daCoreGAV));

        assertTrue(report.getAvailableVersions().containsAll(daCoreVersionsNoBest));
        assertEquals(daCoreGAV, report.getGav());
        assertFalse(report.getBestMatchVersion().isPresent());
        assertTrue(report.getDependencies().isEmpty());
        assertFalse(report.isBlacklisted());
        assertTrue(report.getWhitelisted().isEmpty());

    }

    @Test
    public void testWhiteListedNoBestMatchGAV() throws CommunicationException,
            FindGAVDependencyException {
        List<Product> whitelisted = Arrays.asList(productEAP);
        prepare(whitelisted, false, daCoreVersionsNoBest, null, daCoreNoDT);

        ArtifactReport report = generator.getReport(gavToRequest(daCoreGAV));

        assertTrue(report.getAvailableVersions().containsAll(daCoreVersionsNoBest));
        assertEquals(daCoreGAV, report.getGav());
        assertFalse(report.getBestMatchVersion().isPresent());
        assertTrue(report.getDependencies().isEmpty());
        assertFalse(report.isBlacklisted());
        assertFalse(report.getWhitelisted().isEmpty());
        assertEquals(1, report.getWhitelisted().size());
    }

    @Test
    public void testBlackListedBestMatchGAV() throws CommunicationException,
            FindGAVDependencyException {
        prepare(Collections.emptyList(), true, daCoreVersionsBest, bestMatchVersion, daCoreNoDT);

        ArtifactReport report = generator.getReport(gavToRequest(daCoreGAV));

        assertTrue(report.getAvailableVersions().containsAll(daCoreVersionsNoBest));
        assertEquals(daCoreGAV, report.getGav());
        assertEquals(bestMatchVersion, report.getBestMatchVersion().get());
        assertTrue(report.getDependencies().isEmpty());
        assertTrue(report.isBlacklisted());
        assertTrue(report.getWhitelisted().isEmpty());
    }

    @Test
    public void testArtifactReportShouldNotHaveNullValuesInAvailableVersionsWhenBestMatchVersionIsNull()
            throws CommunicationException, FindGAVDependencyException {
        prepare(Collections.emptyList(), false, daCoreVersionsBest, null, daCoreNoDT);

        ArtifactReport report = generator.getReport(gavToRequest(daCoreGAV));

        assertFalse(report.getBestMatchVersion().isPresent());
        assertFalse(report.getAvailableVersions().stream().anyMatch(version -> version == null));
    }

    @Test
    public void testGetMultipleReport() throws CommunicationException, FindGAVDependencyException {
        prepareMulti();

        ArtifactReport report = generator.getReport(gavToRequest(daCoreGAV));

        assertTrue(report.getAvailableVersions().containsAll(daCoreVersionsNoBest));
        assertEquals(daCoreGAV, report.getGav());
        assertEquals(bestMatchVersion, report.getBestMatchVersion().get());
        assertFalse(report.isBlacklisted());
        assertTrue(report.getWhitelisted().isEmpty());
        assertMultipleDependencies(report.getDependencies());
    }

    /**
     * Test the distinct on a stream in #getLookupReportsForGavs works correctly
     */
    @Test
    public void testDistinctOnGavsStream() {
        // Given
        List<GAV> gavs = new ArrayList<>();
        gavs.add(new GAV("org", "test", "1.0"));
        gavs.add(new GAV("org", "test", "1.1"));
        gavs.add(new GAV("org", "test", "1.0"));

        gavs.add(new GAV("org2", "test2", "2.0"));
        gavs.add(new GAV("org2", "test2", "2.2"));
        gavs.add(new GAV("org2", "test2", "2.0"));
        LookupGAVsRequest request = new LookupGAVsRequest(new HashSet<>(), new HashSet<>(), gavs);

        // When
        List<GAV> uniqueGAVs = new ArrayList<>();
        uniqueGAVs.add(new GAV("org", "test", "1.0"));
        uniqueGAVs.add(new GAV("org", "test", "1.1"));
        uniqueGAVs.add(new GAV("org2", "test2", "2.0"));
        uniqueGAVs.add(new GAV("org2", "test2", "2.2"));

        List<GAV> distinctList = request.getGavs().stream().distinct().collect(Collectors.toList());

        //Then
        assertEquals(uniqueGAVs.size(), distinctList.size());
        assertTrue(uniqueGAVs.equals(distinctList));
    }

    private void assertMultipleDependencies(Set<ArtifactReport> deps) {
        assertEquals(2, deps.size());

        for (ArtifactReport dep : deps) {
            GAV gav = dep.getGav();
            switch (gav.getArtifactId()) {
                case "util": {
                    assertTrue(dep.getAvailableVersions().containsAll(daCoreVersionsBest));
                    assertEquals(daUtilGAV, dep.getGav());
                    assertNotNull(dep.getBestMatchVersion());
                    assertEquals(bestMatchVersion, dep.getBestMatchVersion().get());
                    assertTrue(dep.getDependencies().isEmpty());
                    assertFalse(dep.isBlacklisted());
                    assertTrue(dep.getWhitelisted().isEmpty());
                    break;
                }
                case "common": {
                    assertTrue(dep.getAvailableVersions().containsAll(daCoreVersionsNoBest));
                    assertEquals(daCommonGAV, dep.getGav());
                    assertFalse(dep.getBestMatchVersion().isPresent());
                    assertTrue(dep.getDependencies().isEmpty());
                    assertFalse(dep.isBlacklisted());
                    assertTrue(dep.getWhitelisted().isEmpty());
                    break;
                }
                default: {
                    fail("Unknown artifact id");
                    break;
                }
            }
        }
    }

    private GAVRequest gavToRequest(GAV g) {
        return new GAVRequest(g.getGroupId(), g.getArtifactId(), g.getVersion(), new HashSet<>(),
                new HashSet<>());

    }
}

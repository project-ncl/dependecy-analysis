package org.jboss.da.common.version;

import static org.junit.Assert.*;

import org.jboss.da.common.CommunicationException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public class VersionAnalyzerTest {

    private VersionAnalyzer versionFinder = new VersionAnalyzer(new VersionParser("redhat"));

    private static final String NO_BUILT_VERSION = "1.1.3";

    private static final String NO_BUILT_VERSION_2 = "1.0.20";

    private static final String BUILT_VERSION = "1.1.4";

    private static final String BUILT_VERSION_RH = BUILT_VERSION + "-redhat-20";

    private static final String BUILT_VERSION_2 = "1.1.4.Final";

    private static final String BUILT_VERSION_2_RH = BUILT_VERSION_2 + "-redhat-10";

    private static final String MULTI_BUILT_VERSION = "1.1.5";

    private static final String MULTI_BUILT_VERSION_RH1 = MULTI_BUILT_VERSION + ".redhat-5";

    private static final String MULTI_BUILT_VERSION_RH2 = MULTI_BUILT_VERSION + ".redhat-3";

    private static final String MULTI_BUILT_VERSION_RH_BEST = MULTI_BUILT_VERSION + ".redhat-18";

    private static final String MULTI_BUILT_VERSION_RH4 = MULTI_BUILT_VERSION + ".redhat-16";

    private static final String OTHER_RH_VERSION_1 = "1.0.0.redhat-1";

    private static final String OTHER_RH_VERSION_2 = "1.0.0.redhat-18";

    private static final String OTHER_RH_VERSION_3 = "1.1.1.redhat-15";

    private static final String NON_OSGI_VERSION = "1.3";

    private static final String NON_OSGI_VERSION_RHT = "1.3.redhat-4";

    private static final String NON_OSGI_VERSION_2 = "1.3-Final";

    private static final String NON_OSGI_VERSION_2_RHT = "1.3.0.Final-redhat-7";

    private static final List<String> All_VERSIONS = Arrays.asList(OTHER_RH_VERSION_1,
            OTHER_RH_VERSION_2, NO_BUILT_VERSION_2, NO_BUILT_VERSION, OTHER_RH_VERSION_3,
            MULTI_BUILT_VERSION_RH2, BUILT_VERSION_RH, MULTI_BUILT_VERSION_RH1, BUILT_VERSION_2_RH,
            MULTI_BUILT_VERSION_RH_BEST, MULTI_BUILT_VERSION_RH4, NON_OSGI_VERSION,
            NON_OSGI_VERSION_RHT, BUILT_VERSION_2, NON_OSGI_VERSION_2, NON_OSGI_VERSION_2_RHT);

    private static final List<String> BUILT_VERSIONS = Arrays.asList(OTHER_RH_VERSION_1,
            OTHER_RH_VERSION_2, OTHER_RH_VERSION_3, MULTI_BUILT_VERSION_RH2, BUILT_VERSION_RH,
            MULTI_BUILT_VERSION_RH1, BUILT_VERSION_2_RH, MULTI_BUILT_VERSION_RH_BEST,
            MULTI_BUILT_VERSION_RH4, NON_OSGI_VERSION_RHT, NON_OSGI_VERSION_2_RHT);

    @Test
    public void getBestMatchVersionForNonExistingGAV() throws CommunicationException {
        VersionAnalyzer.VersionAnalysisResult result = versionFinder.analyseVersions("0.0.1",
                Collections.EMPTY_LIST);
        Optional<String> bmv = result.getBestMatchVersion();
        assertFalse("Best match version expected to not be present", bmv.isPresent());
    }

    @Test
    public void getBestMatchVersionForNotBuiltGAV() throws CommunicationException {
        VersionAnalyzer.VersionAnalysisResult result = versionFinder.analyseVersions(
                NO_BUILT_VERSION, All_VERSIONS);
        Optional<String> bmv = result.getBestMatchVersion();
        assertFalse("Best match version expected to not be present", bmv.isPresent());
    }

    @Test
    public void getBestMatchVersionForBuiltGAV() throws CommunicationException {
        checkBMV(BUILT_VERSION_RH, BUILT_VERSION,
                All_VERSIONS.toArray(new String[All_VERSIONS.size()]));
        checkBMV(BUILT_VERSION_2_RH, BUILT_VERSION_2,
                All_VERSIONS.toArray(new String[All_VERSIONS.size()]));
    }

    @Test
    public void getBestMatchVersionForMultipleBuiltGAV() throws CommunicationException {
        checkBMV(MULTI_BUILT_VERSION_RH_BEST, MULTI_BUILT_VERSION,
                All_VERSIONS.toArray(new String[All_VERSIONS.size()]));
    }

    @Test
    public void getBestMatchVersionForNoOSGIGAV() throws CommunicationException {
        checkBMV(NON_OSGI_VERSION_RHT, NON_OSGI_VERSION,
                All_VERSIONS.toArray(new String[All_VERSIONS.size()]));
        checkBMV(NON_OSGI_VERSION_2_RHT, NON_OSGI_VERSION_2,
                All_VERSIONS.toArray(new String[All_VERSIONS.size()]));
    }

    @Test
    public void NCL2931ReproducerTest() {
        String[] avaliableVersions = { "1.4.0.redhat-4", "1.4.redhat-3", "1.4-redhat-2",
                "1.4-redhat-1", "1.6.0.redhat-5", "1.6.0.redhat-4", "1.6.0.redhat-3",
                "1.6.redhat-2", "1.6.redhat-1", "1.9.0.redhat-1", "1.10.0.redhat-5",
                "1.10.0.redhat-4", "1.10.0.redhat-3", "1.10.0.redhat-2", "1.10.0.redhat-1" };
        checkBMV("1.4.0.redhat-4", "1.4", avaliableVersions);
    }

    @Test
    public void ambiguousNonOSGIVersionsTest() {
        String[] avaliableVersionsWithOSGI = { "1.0.0.redhat-1", "1.0.redhat-1", "1.redhat-1" };
        checkBMV("1.0.0.redhat-1", "1.0.0", avaliableVersionsWithOSGI);
        checkBMV("1.0.0.redhat-1", "1.0", avaliableVersionsWithOSGI);
        checkBMV("1.0.0.redhat-1", "1", avaliableVersionsWithOSGI);

        String[] avaliableVersionsWithOSGIRev = { "1.redhat-1", "1.0.redhat-1", "1.0.0.redhat-1" };
        checkBMV("1.0.0.redhat-1", "1.0.0", avaliableVersionsWithOSGIRev);
        checkBMV("1.0.0.redhat-1", "1.0", avaliableVersionsWithOSGIRev);
        checkBMV("1.0.0.redhat-1", "1", avaliableVersionsWithOSGIRev);

        String[] avaliableVersionsWithoutOSGI = { "1.0.redhat-1", "1.redhat-1" };
        checkBMV("1.0.redhat-1", "1.0.0", avaliableVersionsWithoutOSGI);
        checkBMV("1.0.redhat-1", "1.0", avaliableVersionsWithoutOSGI);
        checkBMV("1.0.redhat-1", "1", avaliableVersionsWithoutOSGI);

        String[] avaliableVersionsWithoutOSGI2 = { "1.redhat-1" };
        checkBMV("1.redhat-1", "1.0.0", avaliableVersionsWithoutOSGI2);
        checkBMV("1.redhat-1", "1.0", avaliableVersionsWithoutOSGI2);
        checkBMV("1.redhat-1", "1", avaliableVersionsWithoutOSGI2);
    }

    @Test
    public void nonOSGIVersionsTest() {
        String[] avaliableVersions1 = { "1.0.0.redhat-1", "1.0.redhat-2", "1.redhat-3" };
        checkBMV("1.redhat-3", "1.0.0", avaliableVersions1);
        checkBMV("1.redhat-3", "1.0", avaliableVersions1);
        checkBMV("1.redhat-3", "1", avaliableVersions1);

        String[] avaliableVersions10 = { "1.0.0.redhat-1", "1.0.redhat-3", "1.redhat-2" };
        checkBMV("1.0.redhat-3", "1.0.0", avaliableVersions10);
        checkBMV("1.0.redhat-3", "1.0", avaliableVersions10);
        checkBMV("1.0.redhat-3", "1", avaliableVersions10);

        String[] avaliableVersions100 = { "1.0.0.redhat-3", "1.0.redhat-2", "1.redhat-1" };
        checkBMV("1.0.0.redhat-3", "1.0.0", avaliableVersions100);
        checkBMV("1.0.0.redhat-3", "1.0", avaliableVersions100);
        checkBMV("1.0.0.redhat-3", "1", avaliableVersions100);
    }

    private void checkBMV(String expectedVersion, String version, String[] versions) {
        VersionAnalyzer.VersionAnalysisResult result = versionFinder.analyseVersions(version,
                Arrays.asList(versions));

        Optional<String> bmv = result.getBestMatchVersion();
        assertTrue("Best match version expected to be present", bmv.isPresent());
        assertEquals(expectedVersion, bmv.get());
    }

}

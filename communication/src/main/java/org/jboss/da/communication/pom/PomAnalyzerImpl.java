package org.jboss.da.communication.pom;

import org.jboss.da.communication.pom.api.PomAnalyzer;
import org.apache.commons.io.FileUtils;
import org.commonjava.cartographer.CartoDataException;
import org.commonjava.cartographer.CartographerCore;
import org.commonjava.cartographer.graph.MavenModelProcessor;
import org.commonjava.cartographer.graph.discover.DiscoveryConfig;
import org.commonjava.cartographer.graph.discover.DiscoveryResult;
import org.commonjava.maven.atlas.graph.rel.ProjectRelationship;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.view.MavenPomView;
import org.commonjava.maven.galley.maven.parse.MavenPomReader;
import org.commonjava.maven.galley.maven.parse.PomPeek;
import org.commonjava.maven.galley.maven.util.ArtifactPathUtils;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.communication.pom.model.MavenProject;
import org.jboss.da.communication.pom.model.MavenRepository;
import org.jboss.da.communication.pom.qualifier.DACartographerCore;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.da.communication.CommunicationException;
import org.slf4j.Logger;

@ApplicationScoped
public class PomAnalyzerImpl implements PomAnalyzer {

    @Inject
    private Logger log;

    @Inject
    private PomReader pomReader;

    @Inject
    @DACartographerCore
    private CartographerCore carto;

    @Override
    public GAVDependencyTree readRelationships(File pomRepoDir, File pomPath)
            throws PomAnalysisException {

        try {

            // if pomPath is for e.g <tmp>/application/pom.xml instead of application,
            // rectify this to '<tmp>/application'
            if (pomPath.isFile() && pomPath.getName().equals("pom.xml")) {
                pomPath = pomPath.getParentFile();
            }

            GAVDependencyTree root = null;

            File tempDir = Files.createTempDirectory("deps").toFile();

            Map<GAV, GAVDependencyTree> gavDependencyTreeMap = new HashMap<>();

            try {
                // map of file and ProjectVersionRefs that represent all the pom.xmls found in the pomRepoDir directory
                Map<File, ProjectVersionRef> projectVersionRefs = getProjectVersionRefs(tempDir,
                        findAllPomFiles(pomRepoDir));

                // for each pom.xml
                for (Map.Entry<File, ProjectVersionRef> entry : projectVersionRefs.entrySet()) {

                    // find all the dependencies / plugins / parents / plugins etc for each pom.xml
                    Set<ProjectRelationship<?, ?>> relationships = getDiscoveryResult(tempDir,
                            pomRepoDir, entry.getValue()).getAcceptedRelationships();

                    // convert from ProjectVersionRef to GAV
                    GAV originGAV = generateGAV(entry.getValue());

                    // get the origin GAVDependencyTree
                    GAVDependencyTree originScmGAVTree = addGAVDependencyTreeToMapper(
                            gavDependencyTreeMap, originGAV);

                    // need to use canonical path to get better way of knowing if 2 paths are the same
                    if (pomPath.getCanonicalPath().equals(entry.getKey().getCanonicalPath()))
                        root = originScmGAVTree;

                    for (ProjectRelationship<?, ?> relationship : relationships) {
                        ProjectVersionRef target = relationship.getTarget();

                        GAV targetGAV = generateGAV(target);

                        GAVDependencyTree targetScmGavTree = addGAVDependencyTreeToMapper(
                                gavDependencyTreeMap, targetGAV);

                        addTargetToGAVDependencyTree(originScmGAVTree, targetScmGavTree,
                                relationship);
                    }
                }

                if (root == null) {
                    throw new PomAnalysisException("Root pom was not found in repository.");
                }
                return root;

            } finally {
                // cleanup
                FileUtils.deleteDirectory(tempDir);
            }
        } catch (TransferException | GalleyMavenException | URISyntaxException | CartoDataException
                | IOException e) {
            throw new PomAnalysisException(e);
        }
    }

    @Override
    public GAVDependencyTree readRelationships(File pomRepoDir, GAV gav)
            throws PomAnalysisException {
        try {
            File tempDir = Files.createTempDirectory("deps").toFile();
            try {
                // map of file and ProjectVersionRefs that represent all the pom.xmls found in the pomRepoDir directory
                Map<File, ProjectVersionRef> projectVersionRefs = getProjectVersionRefs(tempDir,
                        findAllPomFiles(pomRepoDir));

                File pomPath = null;

                for (Map.Entry<File, ProjectVersionRef> entry : projectVersionRefs.entrySet()) {
                    if (isProjectVersionRefSameAsGAV(entry.getValue(), gav)) {
                        pomPath = entry.getKey();
                        break;
                    }
                }

                if (pomPath == null) {
                    throw new PomAnalysisException("Could not find the GAV " + gav
                            + " in the project");
                } else {
                    return readRelationships(pomRepoDir, pomPath);
                }

            } finally {
                FileUtils.deleteDirectory(tempDir);
            }
        } catch (TransferException | IOException e) {
            throw new PomAnalysisException(e);
        }
    }

    @Override
    public Optional<File> getPOMFileForGAV(File pomRepoDir, GAV gav) {
        return findAllPomFiles(pomRepoDir).stream()
                .filter(file -> isProjectVersionRefSameAsGAV((new PomPeek(file)).getKey(), gav))
                .findAny();
    }

    /**
     * Return an existing GAVDependencyTree if present in the map, otherwise create a new GAVDependencyTree
     * and map the GAV to the GAVDependencyTree
     *
     * @param map
     * @param gav
     * @return
     */
    private GAVDependencyTree addGAVDependencyTreeToMapper(Map<GAV, GAVDependencyTree> map, GAV gav) {
        return map.computeIfAbsent(gav, (k) -> new GAVDependencyTree(k));
    }

    private GAV generateGAV(ProjectVersionRef ref) {
        return new GAV(ref.getGroupId(), ref.getArtifactId(), ref.getVersionString());
    }

    private DiscoveryResult getDiscoveryResult(File tempDir, File repoDir, ProjectVersionRef ref)
            throws GalleyMavenException, URISyntaxException, CartoDataException {

        MavenPomReader mavenPomReader = carto.getGalley().getPomReader();
        MavenPomView pomView = mavenPomReader.read(ref, getRepoLocations(tempDir, repoDir));

        URI src = new URI(new SimpleLocation("file:" + tempDir.getAbsolutePath()).getUri());

        DiscoveryConfig disConf = new DiscoveryConfig(src);
        disConf.setIncludeBuildSection(false);
        disConf.setIncludeManagedDependencies(false);
        disConf.setIncludeManagedPlugins(false);

        MavenModelProcessor processor = new MavenModelProcessor();
        return processor.readRelationships(pomView, src, disConf);
    }

    private List<Location> getRepoLocations(File cacheDir, File repoDir) {

        List<Location> repoLocations = new LinkedList<>();
        // add local location
        repoLocations.add(new SimpleLocation("file:" + cacheDir.getAbsolutePath()));

        // add maven central location
        repoLocations.add(new SimpleLocation("central", "http://repo.maven.apache.org/maven2/"));

        // add all the repositories mentioned in the pom.xml
        List<SimpleLocation> locationsInPom = getLocationsDefinedInPom(repoDir);
        repoLocations.addAll(locationsInPom);

        return repoLocations;
    }

    private List<SimpleLocation> getLocationsDefinedInPom(File repoDir) {

        List<File> pomFilePaths = findAllPomFiles(repoDir);
        List<SimpleLocation> locations = new LinkedList<>();

        for (File pom : pomFilePaths) {
            Optional<MavenProject> project = pomReader.analyze(pom);
            if (project.isPresent() && project.get().getMavenRepositories() != null) {
                for (MavenRepository repo : project.get().getMavenRepositories()) {
                    locations.add(new SimpleLocation(repo.getId(), repo.getUrl()));
                }
            }
        }

        return locations;
    }

    /**
     * Given the locations of the pom.xml files, setup the tempDir into a structure
     * where Cartographer will be able to analyze the poms, and return back a
     * list of ProjectVersionRefs objects generated from the pom.xml files.
     * @param tempDir
     * @param poms
     * @return
     * @throws TransferException
     * @throws IOException
     */
    private Map<File, ProjectVersionRef> getProjectVersionRefs(File tempDir, List<File> poms)
            throws TransferException, IOException {

        Map<File, ProjectVersionRef> projectVersionRefs = new HashMap<>();

        for (File pomFile : poms) {
            PomPeek peek = new PomPeek(pomFile);

            try {
                String path = ArtifactPathUtils.formatArtifactPath(peek.getKey().asPomArtifact(),
                        carto.getGalley().getTypeMapper());

                File f = new File(tempDir, path);
                f.getParentFile().mkdirs();
                FileUtils.copyFile(pomFile, f);

                projectVersionRefs.put(pomFile.getParentFile().getAbsoluteFile(), peek.getKey());
            } catch (NullPointerException ex) {
                log.warn("Could not parse " + pomFile.getAbsolutePath());
            }
        }
        return projectVersionRefs;
    }

    /**
     * Return all the pom.xml paths found under the folderDir directory
     *
     * @param folderDir directory to analyze dependencies
     * @return List of the pom.xml files
     */
    private List<File> findAllPomFiles(File folderDir) {

        Collection<File> pomFilePaths = FileUtils.listFiles(folderDir, new String[]{"xml"}, true);

        List<File> paths = pomFilePaths.stream()
                                    .filter(f -> f.getName().equals("pom.xml"))
                                    .collect(Collectors.toList());

        return paths;
    }

    /**
     * Based on the target type, add the target to one of the fields in 'origin'
     * @param origin
     * @param target
     * @param relationship
     */
    private void addTargetToGAVDependencyTree(GAVDependencyTree origin, GAVDependencyTree target,
            ProjectRelationship<?, ?> relationship) {
        switch (relationship.getType()) {
            case DEPENDENCY:
                origin.addDependency(target);
                break;
        }
    }

    @Override
    public Optional<MavenProject> readPom(File pomPath) {
        return pomReader.analyze(pomPath);
    }

    @Override
    public Optional<MavenProject> readPom(InputStream is) throws CommunicationException {
        return pomReader.analyze(is);
    }

    private boolean isProjectVersionRefSameAsGAV(ProjectVersionRef f, GAV gav) {
        String version = f.getVersionString();
        String groupId = f.getGroupId();
        String artifactId = f.getArtifactId();

        return gav.getGroupId().equals(groupId) && gav.getArtifactId().equals(artifactId)
                && gav.getVersion().equals(version);
    }
}

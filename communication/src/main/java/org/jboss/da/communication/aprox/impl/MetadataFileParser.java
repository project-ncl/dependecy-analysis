package org.jboss.da.communication.aprox.impl;

import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.model.VersionResponse;
import org.jboss.da.communication.aprox.model.npm.NpmMetadata;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class MetadataFileParser {

    @Inject
    private ObjectMapper om;

    public static VersionResponse parseMavenMetadata(InputStream in) throws IOException, CommunicationException, JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(VersionResponse.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (VersionResponse) jaxbUnmarshaller.unmarshal(in);
    }

    public NpmMetadata parseNpmMetadata(URLConnection connection) throws IOException {
        try (InputStream in = connection.getInputStream()) {
            return om.readValue(in, NpmMetadata.class);
        }
    }
}

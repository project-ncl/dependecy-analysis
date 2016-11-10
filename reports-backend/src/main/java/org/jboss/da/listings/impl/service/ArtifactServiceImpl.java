package org.jboss.da.listings.impl.service;

import org.jboss.da.common.version.VersionParser;
import org.jboss.da.communication.auth.AuthenticatorService;
import org.jboss.da.listings.api.dao.ArtifactDAO;
import org.jboss.da.listings.api.dao.UserDAO;
import org.jboss.da.listings.api.model.Artifact;
import org.jboss.da.listings.api.model.User;
import org.jboss.da.listings.api.service.ArtifactService;

import javax.inject.Inject;

import java.util.List;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public abstract class ArtifactServiceImpl<T extends Artifact> implements ArtifactService<T> {

    @Inject
    protected VersionParser versionParser;

    @Inject
    AuthenticatorService auth;

    @Inject
    private UserDAO users;

    protected abstract ArtifactDAO<T> getDAO();

    protected User currentUser() {
        String username = auth.username().orElseThrow(() -> new IllegalStateException("No logged in user."));
        String userId = auth.userId().orElseThrow(() -> new IllegalStateException("No logged in user."));

        User user = users.findUser(userId).orElseGet(() -> {
            User u = new User(username, userId);
            users.create(u);
            return u;
        });
        if (!user.getUsername().equals(username)) {
            user.setUsername(username);
            users.update(user);
        }
        return user;
    }

    @Override
    public List<T> getAll() {
        return getDAO().findAll();
    }

}

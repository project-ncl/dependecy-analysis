package org.jboss.da.communication.repository.api;

import org.jboss.da.common.CommunicationException;

/**
 * Exception thrown when there is error in communication with artifact repository.
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
public class RepositoryException extends CommunicationException {

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

}

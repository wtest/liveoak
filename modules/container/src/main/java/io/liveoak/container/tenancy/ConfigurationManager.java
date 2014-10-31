package io.liveoak.container.tenancy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface ConfigurationManager {

    public String type();

    public String versionedResourcePath();

    public boolean versioned();

    public void removeResource(String id, String type) throws Exception;

    public void updateResource(String id, String type, JsonNode config) throws Exception;

    public ObjectNode readResource(String id, String type) throws Exception;

}

package org.projectodd.restafari.spi;

import java.util.stream.Stream;

/** Object to report on results of actions performed by Resources.
 *
 * @author Bob McWhirter
 */
public interface Responder {

    /** Report a resource that has been read in response to a read request.
     *
     * @param resource The resource that has been fetched for reading.
     */
    void resourceRead(Resource resource);

    /** Report a resource that has been created in response to a create (or update) request.
     *
     * @param resource The resource that has been created
     */
    void resourceCreated(Resource resource);

    /** Report a resource that has been deleted in response to a delete request.
     *
     * <p>When reporting a deleted resource, the expectation is that the deleted
     * resource will continue to have state available until fully notifying
     * all clients and interested parties.</p>
     *
     * @param resource The resource that has been deleted.
     */
    void resourceDeleted(Resource resource);

    /** Report a resource that has been updated in response to an update request.
     *
     * @param resource The resource that has been updated.
     */
    void resourceUpdated(Resource resource);

    /** Indicate that a resource does not support create requests.
     *
     * @param resource The resource that does not support creating requests.
     */
    void createNotSupported(Resource resource);

    /** Indicate that a resource does not support read requests.
     *
     * @param resource The resource that does not support reading requests.
     */
    void readNotSupported(Resource resource);

    /** Indicate that a resource does not support update requests.
     *
     * @param resource The resource that does not support update requests.
     */
    void updateNotSupported(Resource resource);

    /** Indicate that a resource does not support delete requests.
     *
     * @param resource The resource that does not support delete requests.
     */
    void deleteNotSupported(Resource resource);

    /** Indicate an attempt to manipulate a resource that cannot be found.
     *
     * @param id The identifier that cannot be found.
     */
    void noSuchResource(String id);

}

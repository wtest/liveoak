package io.liveoak.container.service.bootstrap;

import io.liveoak.client.DefaultClient;
import io.liveoak.container.service.ClientConnectorService;
import io.liveoak.container.service.ClientService;
import org.jboss.logging.Logger;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

import static io.liveoak.spi.Services.CLIENT;
import static io.liveoak.spi.Services.server;

/**
 * @author Bob McWhirter
 */
public class ClientBootstrappingService implements Service<Void> {

    @Override
    public void start(StartContext context) throws StartException {
        log.debug("bootstrap client");
        ServiceTarget target = context.getChildTarget();

        target.addListener(new AbstractServiceListener<Object>() {
            @Override
            public void transition(ServiceController<?> controller, ServiceController.Transition transition) {
                log.trace(controller.getName() + " // " + transition);
            }
        });

        ClientService client = new ClientService();
        target.addService(CLIENT, client)
                .install();

        ClientConnectorService clientConnector = new ClientConnectorService();
        target.addService(CLIENT.append("connect"), clientConnector)
                .addDependency(CLIENT, DefaultClient.class, clientConnector.clientInjector())
                .addDependency(server("local", false))
                .install();

    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    private static final Logger log = Logger.getLogger(ClientBootstrappingService.class);
}

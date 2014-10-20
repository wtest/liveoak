package io.liveoak.container.extension;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.spi.resource.MountPointResource;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 */
public class SystemExtensionContextImpl implements SystemExtensionContext {

    public SystemExtensionContextImpl(ServiceTarget target, String moduleId, String id, ServiceName systemExtensionMount, ObjectNode configuration) {
        this.target = target;
        this.id = id;
        this.moduleId = moduleId;
        this.systemExtensionMount = systemExtensionMount;
        this.configuration = configuration;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public String moduleId() {
        return this.moduleId;
    }

    @Override
    public ServiceTarget target() {
        return this.target;
    }

    @Override
    public void mountPrivate(RootResource resource) {
        target.addService(Services.systemResource(moduleId, this.id), new ValueService<RootResource>(new ImmediateValue<>(resource)))
                .install();

        mountPrivate(Services.systemResource(moduleId, this.id));
    }

    @Override
    public void mountPrivate(ServiceName privateName) {
        MountService<RootResource> mount = new MountService(this.id);

        InitializeResourceService configApply = new InitializeResourceService();
        target.addService(privateName.append("apply-config"), configApply)
                .addDependency(privateName, RootResource.class, configApply.resourceInjector())
                .addInjection(configApply.configurationInjector(), this.configuration)
                .install();

        RootResourceLifecycleService lifecycle = new RootResourceLifecycleService();
        target.addService(privateName.append("lifecycle"), lifecycle)
                .addDependency(privateName.append("apply-config"))
                .addDependency(privateName, RootResource.class, lifecycle.resourceInjector())
                .install();

        ServiceController<? extends Resource> controller = this.target.addService(privateName.append("mount"), mount)
                .addDependency(privateName.append("lifecycle"))
                .addDependency(this.systemExtensionMount, MountPointResource.class, mount.mountPointInjector())
                .addDependency(privateName, RootResource.class, mount.resourceInjector())
                .install();
    }

    public void mountInstance(RootResource resource) {
        target.addService(Services.instanceResource(this.id), new ValueService<RootResource>(new ImmediateValue<>(resource)))
                .install();

        mountPrivate(Services.instanceResource(this.id));
    }

    public void mountInstance(ServiceName privateName) {
        MountService<RootResource> mount = new MountService();

        InitializeResourceService configApply = new InitializeResourceService();
        target.addService(privateName.append("apply-config"), configApply)
                .addDependency(privateName, RootResource.class, configApply.resourceInjector())
                .addInjection(configApply.configurationInjector(), this.configuration)
                .install();

        RootResourceLifecycleService lifecycle = new RootResourceLifecycleService();
        target.addService(privateName.append("lifecycle"), lifecycle)
                .addDependency(privateName.append("apply-config"))
                .addDependency(privateName, RootResource.class, lifecycle.resourceInjector())
                .install();

        ServiceController<? extends Resource> controller = this.target.addService(privateName.append("mount"), mount)
                .addDependency(privateName.append("lifecycle"))
                .addDependency(this.systemExtensionMount, MountPointResource.class, mount.mountPointInjector())
                .addDependency(privateName, RootResource.class, mount.resourceInjector())
                .install();
    }

    private ServiceTarget target;
    private String moduleId;
    private String id;
    private ServiceName systemExtensionMount;
    private ObjectNode configuration;


}

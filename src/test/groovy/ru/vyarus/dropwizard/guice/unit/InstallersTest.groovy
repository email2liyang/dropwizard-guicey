package ru.vyarus.dropwizard.guice.unit

import io.dropwizard.lifecycle.Managed
import io.dropwizard.setup.Environment
import org.eclipse.jetty.util.component.LifeCycle
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.TaskInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingletonInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.health.HealthCheckInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.plugin.PluginInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller
import ru.vyarus.dropwizard.guice.module.installer.install.TypeInstaller
import ru.vyarus.dropwizard.guice.support.feature.*
import ru.vyarus.dropwizard.guice.support.feature.abstr.*
import spock.lang.Unroll

/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class InstallersTest extends AbstractTest {

    @Unroll("Check #installer.simpleName")
    def "check installers"() {

        setup:
        Environment environment = mockEnvironment()
        interaction {
            switch (installer) {
                case TaskInstaller:
                    1 * environment.admin().addTask(_)
                    break;
                case ManagedInstaller:
                    1 * environment.lifecycle().manage(_ as Managed)
                    break;
                case LifeCycleInstaller:
                    1 * environment.lifecycle().manage(_ as LifeCycle)
                    break;
                case JerseyProviderInstaller:
                    1 * environment.jersey().register(_)
                    break;
                case HealthCheckInstaller:
                    1 * environment.healthChecks().register(*_)
                    break;
            }
        }

        expect: "installer did not accept abstract class and correctly installs good one"
        def inst = installer.newInstance()
        inst.matches(goodBean)
        !inst.matches(denyBean)
        if (inst instanceof TypeInstaller)
            inst.install(environment, goodBean)
        if (inst instanceof InstanceInstaller)
            inst.install(environment, goodBean.newInstance())

        where:
        installer               | goodBean             | denyBean
        TaskInstaller           | DummyTask            | AbstractTask
        ResourceInstaller       | DummyResource        | AbstractResource
        ManagedInstaller        | DummyManaged         | AbstractManaged
        LifeCycleInstaller      | DummyLifeCycle       | AbstractLifeCycle
        JerseyProviderInstaller | DummyExceptionMapper | AbstractJerseyProvider
        JerseyProviderInstaller | DummyJerseyProvider  | AbstractJerseyInjectableProvider
        HealthCheckInstaller    | DummyHealthCheck     | AbstractHealthCheck
        EagerSingletonInstaller | DummyService         | AbstractService
        PluginInstaller         | DummyPlugin1         | AbstractPlugin
    }
}
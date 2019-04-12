package net.troja.trial.ticketstats;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.DefaultsProviderPlugin;
import springfox.documentation.spi.service.ResourceGroupingStrategy;
import springfox.documentation.spi.service.contexts.DocumentationContextBuilder;
import springfox.documentation.spring.web.SpringGroupingStrategy;
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager;

/**
 * This class is used to fix incompatibilities between Springfox and Spring Boot 2.2
 * see https://github.com/springfox/springfox/issues/2932
 */
@Component
@Primary
public class DocumentationPluginsManagerBootAdapter extends DocumentationPluginsManager {
    @Autowired
    @Qualifier("resourceGroupingStrategyRegistry")
    private PluginRegistry<ResourceGroupingStrategy, DocumentationType> resourceGroupingStrategies;

    @Autowired
    @Qualifier("defaultsProviderPluginRegistry")
    private PluginRegistry<DefaultsProviderPlugin, DocumentationType> defaultsProviders;

    @Override
    public ResourceGroupingStrategy resourceGroupingStrategy(DocumentationType documentationType) {
        return resourceGroupingStrategies.getPluginOrDefaultFor(documentationType, new SpringGroupingStrategy());
    }

    @Override
    public DocumentationContextBuilder createContextBuilder(DocumentationType documentationType, DefaultsProviderPlugin defaultConfiguration) {
        return defaultsProviders.getPluginOrDefaultFor(documentationType, defaultConfiguration)
                .create(documentationType).withResourceGroupingStrategy(resourceGroupingStrategy(documentationType));
    }
}


package net.neoforged.gradle.foxlauncher.extensions;

import net.minecraftforge.gdi.BaseDSLElement;
import net.minecraftforge.gdi.ConfigurableDSLElement;
import net.minecraftforge.gdi.annotations.ProjectGetter;
import net.neoforged.gradle.dsl.common.extensions.obfuscation.Obfuscation;
import net.neoforged.gradle.dsl.common.tasks.WithOutput;
import net.neoforged.gradle.dsl.common.util.DistributionType;
import net.neoforged.gradle.foxlauncher.model.DynamicProjectType;
import net.neoforged.gradle.foxlauncher.tasks.SetupProjectFromRuntime;
import net.neoforged.gradle.mcp.McpProjectPlugin;
import net.neoforged.gradle.mcp.runtime.extensions.McpRuntimeExtension;
import net.neoforged.gradle.vanilla.VanillaProjectPlugin;
import net.neoforged.gradle.vanilla.runtime.VanillaRuntimeDefinition;
import net.neoforged.gradle.vanilla.runtime.extensions.VanillaRuntimeExtension;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

public abstract class DynamicProjectExtension implements BaseDSLElement<DynamicProjectExtension> {

    private final Project project;

    @Nullable
    private DynamicProjectType type = null;

    @Inject
    public DynamicProjectExtension(Project project) {
        this.project = project;
    }

    @ProjectGetter
    @Override
    public Project getProject() {
        return project;
    }

    public void clean(final String minecraftVersion) {
        type = DynamicProjectType.CLEAN;

        project.getPlugins().apply(VanillaProjectPlugin.class);
        configureCommon();

        final JavaPluginExtension javaPluginExtension = getProject().getExtensions().getByType(JavaPluginExtension.class);
        final SourceSet mainSource = javaPluginExtension.getSourceSets().getByName("main");

        final VanillaRuntimeExtension vanillaRuntimeExtension = project.getExtensions().getByType(VanillaRuntimeExtension.class);
        final VanillaRuntimeDefinition runtimeDefinition = vanillaRuntimeExtension.create(builder -> {
            builder.withMinecraftVersion(minecraftVersion)
                    .withDistributionType(DistributionType.CLIENT)
                    .withName(project.getName());
        });

        final TaskProvider<SetupProjectFromRuntime> setup = project.getTasks().register("setup", SetupProjectFromRuntime.class, task -> {
            task.getSourcesFile().set(runtimeDefinition.getSourceJarTask().flatMap(WithOutput::getOutput));
        });

        final Configuration implementation = project.getConfigurations().getByName(mainSource.getImplementationConfigurationName());
        runtimeDefinition.getMinecraftDependenciesConfiguration().getAllDependencies()
                .forEach(dep -> implementation.getDependencies().add(dep));
    }

    private void configureCommon() {
        final Obfuscation obfuscation = project.getExtensions().getByType(Obfuscation.class);
        obfuscation.getCreateAutomatically().set(false);
    }

    @NotNull
    public DynamicProjectType getType() {
        if (type == null)
            throw new IllegalStateException("Project is not configured yet!");
        return type;
    }
}

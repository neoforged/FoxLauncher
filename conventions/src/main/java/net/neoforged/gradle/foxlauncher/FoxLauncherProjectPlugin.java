package net.neoforged.gradle.foxlauncher;

import net.neoforged.gradle.foxlauncher.extensions.DynamicProjectExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public class FoxLauncherProjectPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project target) {
        target.getExtensions().create("dynamicProject", DynamicProjectExtension.class, target);
    }
}

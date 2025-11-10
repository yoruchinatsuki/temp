package com.autoshutdown.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            // For now, return null since we're using NeoForge's config system
            // This could be extended to provide a GUI config screen
            return null;
        };
    }
}
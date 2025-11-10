package com.autoshutdown.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class AutoShutdownConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec COMMON_SPEC;
    
    public static final ModConfigSpec.ConfigValue<String> commandName;
    public static final ModConfigSpec.ConfigValue<Integer> waitTimeMinutes;
    public static final ModConfigSpec.ConfigValue<Boolean> shutdownComputer;
    public static final ModConfigSpec.ConfigValue<String> messagePrefix;
    
    static {
        BUILDER.push("General Settings");
        
        commandName = BUILDER
            .comment("The command name to trigger auto shutdown detection")
            .define("commandName", "autoshutdown");
        
        waitTimeMinutes = BUILDER
            .comment("Time to wait in minutes before shutting down when no players are online")
            .defineInRange("waitTimeMinutes", 5, 1, 60);
        
        shutdownComputer = BUILDER
            .comment("Whether to shutdown the computer after closing the server")
            .define("shutdownComputer", false);
        
        messagePrefix = BUILDER
            .comment("Message prefix for all auto shutdown messages")
            .define("messagePrefix", "[AutoShutdown] ");
        
        BUILDER.pop();
        
        COMMON_SPEC = BUILDER.build();
    }
}
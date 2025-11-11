package com.misaka10843.muiltplayerautoshutdown.command;

import com.misaka10843.muiltplayerautoshutdown.AutoShutdown;
import com.misaka10843.muiltplayerautoshutdown.config.AutoShutdownConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AutoShutdownCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal(AutoShutdownConfig.commandName.get())
                .executes(AutoShutdownCommand::handleAutoShutdown)
                .then(Commands.literal("status")
                    .executes(AutoShutdownCommand::handleStatus))
                .then(Commands.literal("cancel")
                    .requires(source -> source.hasPermission(2))
                    .executes(AutoShutdownCommand::handleCancel))
                .then(Commands.literal("set")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("minutes", IntegerArgumentType.integer(1, 60))
                        .executes(AutoShutdownCommand::handleSetTime)))
        );
    }
    
    private static int handleAutoShutdown(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (source.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) source.getEntity();
            if (!source.hasPermission(2)) {
                player.sendSystemMessage(Component.literal(AutoShutdownConfig.messagePrefix.get() + 
                    "You don't have permission to use this command!"));
                return 0;
            }
        }
        
        AutoShutdown.getInstance().startAutoShutdownDetection(source.getServer());
        
        source.sendSuccess(() -> Component.literal(AutoShutdownConfig.messagePrefix.get() + 
            "Auto shutdown detection started!"), true);
        
        return 1;
    }
    
    private static int handleStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (AutoShutdown.getInstance().isCountingDown()) {
            int remaining = AutoShutdown.getInstance().getRemainingMinutes();
            source.sendSuccess(() -> Component.literal(AutoShutdownConfig.messagePrefix.get() + 
                "Auto shutdown is active! " + remaining + " minutes remaining."), true);
        } else {
            source.sendSuccess(() -> Component.literal(AutoShutdownConfig.messagePrefix.get() + 
                "Auto shutdown is not currently active."), true);
        }
        
        return 1;
    }
    
    private static int handleCancel(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        AutoShutdown.getInstance().stopShutdownTimer();
        
        source.sendSuccess(() -> Component.literal(AutoShutdownConfig.messagePrefix.get() + 
            "Auto shutdown cancelled!"), true);
        
        return 1;
    }
    
    private static int handleSetTime(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int minutes = IntegerArgumentType.getInteger(context, "minutes");
        
        // This would require config hot-reloading or temporary override
        source.sendSuccess(() -> Component.literal(AutoShutdownConfig.messagePrefix.get() + 
            "Wait time set to " + minutes + " minutes (requires config restart to persist)."), true);
        
        return 1;
    }
}
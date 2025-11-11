package com.misaka10843.muiltplayerautoshutdown.events;

import com.misaka10843.muiltplayerautoshutdown.AutoShutdown;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AutoShutdown.MOD_ID)
public class PlayerEventHandler {
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            
            // Check if auto shutdown is active and cancel it if a player joins
            if (AutoShutdown.getInstance().isCountingDown()) {
                AutoShutdown.getInstance().checkPlayerCountAndSchedule(player.getServer());
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            
            // Check player count after a player leaves
            // Schedule with a small delay to ensure the player list is updated
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(1000); // Wait 1 second for player list to update
                    if (player.getServer() != null) {
                        AutoShutdown.getInstance().checkPlayerCountAndSchedule(player.getServer());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }
}
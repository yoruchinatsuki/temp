package com.misaka10843.muiltplayerautoshutdown;

import com.misaka10843.muiltplayerautoshutdown.config.AutoShutdownConfig;
import com.misaka10843.muiltplayerautoshutdown.events.PlayerEventHandler;
import net.minecraft.commands.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod(AutoShutdown.MOD_ID)
public class AutoShutdown {
    public static final String MOD_ID = "autoshutdown";
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoShutdown.class);

    private static AutoShutdown instance;
    private Timer shutdownTimer;
    private final AtomicBoolean isCountingDown = new AtomicBoolean(false);
    private int remainingMinutes = 0;
    private boolean isSinglePlayer = false;

    public AutoShutdown(IEventBus modEventBus) {
        instance = this;

        // Register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AutoShutdownConfig.COMMON_SPEC);

        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(PlayerEventHandler::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(PlayerEventHandler::onPlayerLoggedOut);

        // Set up config directory
        File configDir = FMLPaths.CONFIGDIR.get().toFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("AutoShutdown mod initialized");
    }

    private void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        isSinglePlayer = server.isSingleplayer();
        LOGGER.info("AutoShutdown: Server type detected - " + (isSinglePlayer ? "Single Player" : "Dedicated Server"));
    }

    private void onServerStopping(ServerStoppingEvent event) {
        stopShutdownTimer();
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        com.autoshutdown.command.AutoShutdownCommand.register(dispatcher);
    }

    public static AutoShutdown getInstance() {
        return instance;
    }

    public void startAutoShutdownDetection(MinecraftServer server) {
        if (isSinglePlayer) {
            server.getPlayerList().getPlayers().stream()
                    .findFirst()
                    .ifPresent(player -> player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal(AutoShutdownConfig.messagePrefix.get() +
                                    "Auto shutdown is not supported in single player mode.")));
            return;
        }

        checkPlayerCountAndSchedule(server);
    }

    public void checkPlayerCountAndSchedule(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        int playerCount = players.size();

        // For single player host, check if only host remains (count < 1 after removing host)
        // For dedicated server, check if no players remain (count = 0)
        boolean shouldShutdown = isSinglePlayer ? playerCount <= 1 : playerCount == 0;

        if (shouldShutdown && !isCountingDown.get()) {
            startShutdownCountdown(server);
        } else if (!shouldShutdown && isCountingDown.get()) {
            stopShutdownTimer();
            broadcastMessage(server, AutoShutdownConfig.messagePrefix.get() +
                    "Auto shutdown cancelled - players detected!");
        }
    }

    private void startShutdownCountdown(MinecraftServer server) {
        if (isCountingDown.compareAndSet(false, true)) {
            remainingMinutes = AutoShutdownConfig.waitTimeMinutes.get();
            shutdownTimer = new Timer("AutoShutdown-Timer", true);

            broadcastMessage(server, AutoShutdownConfig.messagePrefix.get() +
                    "Server will shut down in " + remainingMinutes + " minutes due to inactivity!");

            TimerTask countdownTask = new TimerTask() {
                @Override
                public void run() {
                    if (remainingMinutes > 0) {
                        if (remainingMinutes == 1) {
                            broadcastMessage(server, AutoShutdownConfig.messagePrefix.get() +
                                    "WARNING: Server will shut down in 1 minute!");
                        } else if (remainingMinutes % 1 == 0) {
                            broadcastMessage(server, AutoShutdownConfig.messagePrefix.get() +
                                    "Server will shut down in " + remainingMinutes + " minutes!");
                        }
                        remainingMinutes--;
                    } else {
                        executeShutdown(server);
                    }
                }
            };

            shutdownTimer.scheduleAtFixedRate(countdownTask, 0, 60 * 1000); // Every minute
        }
    }

    public void stopShutdownTimer() {
        if (shutdownTimer != null) {
            shutdownTimer.cancel();
            shutdownTimer = null;
        }
        isCountingDown.set(false);
        remainingMinutes = 0;
    }

    private void executeShutdown(MinecraftServer server) {
        broadcastMessage(server, AutoShutdownConfig.messagePrefix.get() + "Saving world and shutting down...");

        // Save the world
        server.saveAllChunks(true, true, true);

        // Stop the timer
        stopShutdownTimer();

        // Schedule shutdown to run after a short delay to ensure messages are sent
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // Stop the server
                server.halt(false);

                // Optionally shutdown computer if enabled
                if (AutoShutdownConfig.shutdownComputer.get()) {
                    shutdownComputer();
                }
            }
        }, 2000); // 2 second delay
    }

    private void shutdownComputer() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            if (os.contains("win")) {
                pb = new ProcessBuilder("shutdown", "/s", "/t", "30");
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("sudo", "shutdown", "-h", "+1");
            } else {
                // Linux/Unix
                pb = new ProcessBuilder("sudo", "shutdown", "-h", "+1");
            }

            pb.start();
            LOGGER.info("Computer shutdown command executed");
        } catch (Exception e) {
            LOGGER.error("Failed to execute computer shutdown command", e);
        }
    }

    private void broadcastMessage(MinecraftServer server, String message) {
        server.getPlayerList().getPlayers().forEach(player ->
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message)));
        LOGGER.info(message);
    }

    public boolean isCountingDown() {
        return isCountingDown.get();
    }

    public int getRemainingMinutes() {
        return remainingMinutes;
    }
}
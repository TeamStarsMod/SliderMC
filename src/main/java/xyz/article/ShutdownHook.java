package xyz.article;

public class ShutdownHook {
    public ShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!RunningData.stopping) {
                MinecraftServer.stop();
            }
        }));
    }
}

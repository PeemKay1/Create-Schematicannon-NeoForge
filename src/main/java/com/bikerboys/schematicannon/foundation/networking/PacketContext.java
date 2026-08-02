package com.bikerboys.schematicannon.foundation.networking;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.server.level.ServerPlayer;

/** Small compatibility surface that keeps packet logic independent of Fabric internals. */
public final class PacketContext {
    private final ServerPlayer sender;
    private final Executor executor;

    public PacketContext(ServerPlayer sender, Executor executor) {
        this.sender = sender;
        this.executor = executor;
    }

    public CompletableFuture<Void> enqueueWork(Runnable work) {
        return CompletableFuture.runAsync(work, executor);
    }

    public ServerPlayer getSender() {
        return sender;
    }
}

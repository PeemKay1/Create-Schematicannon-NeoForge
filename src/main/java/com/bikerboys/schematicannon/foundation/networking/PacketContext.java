package com.bikerboys.schematicannon.foundation.networking;

import java.util.concurrent.CompletableFuture;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Small compatibility surface that keeps packet logic independent of NeoForge internals. */
public final class PacketContext {
    private final IPayloadContext delegate;

    public PacketContext(IPayloadContext delegate) {
        this.delegate = delegate;
    }

    public CompletableFuture<Void> enqueueWork(Runnable work) {
        return delegate.enqueueWork(work);
    }

    public ServerPlayer getSender() {
        return delegate.player() instanceof ServerPlayer player ? player : null;
    }
}

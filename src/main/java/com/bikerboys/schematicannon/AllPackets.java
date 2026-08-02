package com.bikerboys.schematicannon;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.bikerboys.schematicannon.content.equipment.blueprint.BlueprintAssignCompleteRecipePacket;
import com.bikerboys.schematicannon.content.equipment.clipboard.ClipboardEditPacket;
import com.bikerboys.schematicannon.content.schematics.cannon.ConfigureSchematicannonPacket;
import com.bikerboys.schematicannon.content.schematics.packet.InstantSchematicPacket;
import com.bikerboys.schematicannon.content.schematics.packet.SchematicPlacePacket;
import com.bikerboys.schematicannon.content.schematics.packet.SchematicSyncPacket;
import com.bikerboys.schematicannon.content.schematics.packet.SchematicUploadPacket;
import com.bikerboys.schematicannon.foundation.gui.menu.ClearMenuPacket;
import com.bikerboys.schematicannon.foundation.gui.menu.GhostItemSubmitPacket;
import com.bikerboys.schematicannon.foundation.networking.ISyncPersistentData;
import com.bikerboys.schematicannon.foundation.networking.PacketContext;
import com.bikerboys.schematicannon.foundation.networking.SimplePacketBase;
import com.bikerboys.schematicannon.foundation.utility.ServerSpeedProvider;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Network protocol for Schematicannon's gameplay and GUI synchronization. */
public enum AllPackets {
    CONFIGURE_SCHEMATICANNON(ConfigureSchematicannonPacket.class, ConfigureSchematicannonPacket::new, Side.SERVER),
    PLACE_SCHEMATIC(SchematicPlacePacket.class, SchematicPlacePacket::new, Side.SERVER),
    UPLOAD_SCHEMATIC(SchematicUploadPacket.class, SchematicUploadPacket::new, Side.SERVER),
    CLEAR_CONTAINER(ClearMenuPacket.class, ClearMenuPacket::new, Side.SERVER),
    BLUEPRINT_COMPLETE_RECIPE(BlueprintAssignCompleteRecipePacket.class, BlueprintAssignCompleteRecipePacket::new, Side.SERVER),
    INSTANT_SCHEMATIC(InstantSchematicPacket.class, InstantSchematicPacket::new, Side.SERVER),
    SYNC_SCHEMATIC(SchematicSyncPacket.class, SchematicSyncPacket::new, Side.SERVER),
    SUBMIT_GHOST_ITEM(GhostItemSubmitPacket.class, GhostItemSubmitPacket::new, Side.SERVER),
    CLIPBOARD_EDIT(ClipboardEditPacket.class, ClipboardEditPacket::new, Side.SERVER),
    SERVER_SPEED(ServerSpeedProvider.Packet.class, ServerSpeedProvider.Packet::new, Side.CLIENT),
    PERSISTENT_DATA(ISyncPersistentData.PersistentDataPacket.class, ISyncPersistentData.PersistentDataPacket::new, Side.CLIENT);

    public static final String NETWORK_VERSION = "4";
    private static final Map<Class<?>, AllPackets> BY_CLASS = new HashMap<>();
    private static final ChannelFacade CHANNEL = new ChannelFacade();

    static {
        for (AllPackets packet : values())
            BY_CLASS.put(packet.javaType, packet);
    }

    private final Class<? extends SimplePacketBase> javaType;
    private final Function<RegistryFriendlyByteBuf, ? extends SimplePacketBase> decoder;
    private final CustomPacketPayload.Type<SimplePacketBase> payloadType;
    private final StreamCodec<RegistryFriendlyByteBuf, SimplePacketBase> codec;
    private final Side side;

    <T extends SimplePacketBase> AllPackets(Class<T> javaType, Function<RegistryFriendlyByteBuf, T> decoder, Side side) {
        this.javaType = javaType;
        this.decoder = decoder;
        this.side = side;
        this.payloadType = new CustomPacketPayload.Type<>(Schematicannon.asResource(name().toLowerCase(java.util.Locale.ROOT)));
        this.codec = StreamCodec.ofMember(SimplePacketBase::write, buffer -> this.decoder.apply(buffer));
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Schematicannon.ID).versioned(NETWORK_VERSION);
        for (AllPackets packet : values())
            if (packet.side == Side.SERVER)
                registrar.playToServer(packet.payloadType, packet.codec,
                        (payload, context) -> payload.handle(new PacketContext(context)));
            else
                registrar.playToClient(packet.payloadType, packet.codec);
    }

    public static void registerClient(RegisterClientPayloadHandlersEvent event) {
        for (AllPackets packet : values())
            if (packet.side == Side.CLIENT)
                event.register(packet.payloadType,
                        (payload, context) -> payload.handle(new PacketContext(context)));
    }

    public static CustomPacketPayload.Type<SimplePacketBase> typeOf(Class<?> packetClass) {
        AllPackets packet = BY_CLASS.get(packetClass);
        if (packet == null)
            throw new IllegalArgumentException("Unregistered packet class: " + packetClass.getName());
        return packet.payloadType;
    }

    public static ChannelFacade getChannel() {
        return CHANNEL;
    }

    public static final class ChannelFacade {
        public void sendToServer(SimplePacketBase packet) {
            ClientPacketDistributor.sendToServer(packet);
        }
    }

    private enum Side { CLIENT, SERVER }
}

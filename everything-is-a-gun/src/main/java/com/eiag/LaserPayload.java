package com.eiag;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** Server-authoritative start/end points for one short-lived client beam. */
public record LaserPayload(Vec3 start, Vec3 end) implements CustomPacketPayload {
    public static final Type<LaserPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("eiag", "laser"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LaserPayload> CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeDouble(payload.start.x);
                buffer.writeDouble(payload.start.y);
                buffer.writeDouble(payload.start.z);
                buffer.writeDouble(payload.end.x);
                buffer.writeDouble(payload.end.y);
                buffer.writeDouble(payload.end.z);
            },
            buffer -> new LaserPayload(
                    new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                    new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())));

    @Override
    public Type<LaserPayload> type() {
        return TYPE;
    }
}

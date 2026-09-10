package com.eiag;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Exact server-calculated view kick for the player who fired. */
public record RecoilPayload(float yawDegrees, float pitchDegrees) implements CustomPacketPayload {
    public static final Type<RecoilPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("eiag", "recoil"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecoilPayload> CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeFloat(payload.yawDegrees);
                buffer.writeFloat(payload.pitchDegrees);
            },
            buffer -> new RecoilPayload(buffer.readFloat(), buffer.readFloat()));

    @Override public Type<RecoilPayload> type() { return TYPE; }
}

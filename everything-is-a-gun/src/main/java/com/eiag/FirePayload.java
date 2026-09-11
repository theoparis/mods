package com.eiag;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;

/** Target-independent client fire request; damage and cooldown remain server-authoritative. */
public record FirePayload(InteractionHand hand, float yawDegrees, float pitchDegrees) implements CustomPacketPayload {
    public static final Type<FirePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("eiag", "fire"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FirePayload> CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeBoolean(payload.hand == InteractionHand.OFF_HAND);
                buffer.writeFloat(payload.yawDegrees);
                buffer.writeFloat(payload.pitchDegrees);
            },
            buffer -> new FirePayload(buffer.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND,
                    buffer.readFloat(), buffer.readFloat()));
    @Override public Type<FirePayload> type() { return TYPE; }
}

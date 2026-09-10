package com.eiag;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;

/** Client fire request used when an entity interaction would otherwise be range-limited. */
public record FirePayload(InteractionHand hand) implements CustomPacketPayload {
    public static final Type<FirePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("eiag", "fire"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FirePayload> CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeBoolean(payload.hand == InteractionHand.OFF_HAND),
            buffer -> new FirePayload(buffer.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
    @Override public Type<FirePayload> type() { return TYPE; }
}

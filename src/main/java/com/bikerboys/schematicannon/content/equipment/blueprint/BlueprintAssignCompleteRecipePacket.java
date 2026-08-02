package com.bikerboys.schematicannon.content.equipment.blueprint;

import com.bikerboys.schematicannon.foundation.networking.SimplePacketBase;
import com.bikerboys.schematicannon.foundation.networking.PacketContext;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;


public class BlueprintAssignCompleteRecipePacket extends SimplePacketBase {

	private final Identifier recipeID;

	public BlueprintAssignCompleteRecipePacket(Identifier recipeID) {
		this.recipeID = recipeID;
	}

	public BlueprintAssignCompleteRecipePacket(FriendlyByteBuf buffer) {
		recipeID = buffer.readIdentifier();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeIdentifier(recipeID);
	}

	@Override
	public boolean handle(PacketContext context) {
		return false;
	}


}

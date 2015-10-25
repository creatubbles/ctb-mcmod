package com.creatubbles.ctbmod.common.command;

import javax.ws.rs.core.Response;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import com.creatubbles.api.CreatubblesAPI;
import com.creatubbles.api.request.creator.UsersCreatorsRequest;
import com.creatubbles.api.response.creator.UserCreatorsResponse;
import com.creatubbles.repack.endercore.common.util.ChatUtil;

public class CommandGetCreators extends CommandBase {

	@Override
	public String getCommandName() {
		return "ctb-creators";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		return "/ctb-creators";
	}

	@Override
	public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
		UsersCreatorsRequest req = new UsersCreatorsRequest("me", CommandLogin.accessToken);
		Response resp = req.execute();
		if (resp.hasEntity()) {
			UserCreatorsResponse creators = CreatubblesAPI.GSON.fromJson(resp.readEntity(String.class), UserCreatorsResponse.class);
			p_71515_1_.addChatMessage(ChatUtil.wrap("Found creators: " + creators.creators));
		} else {
			ChatUtil.sendNoSpamClient("Error: " + resp.getStatusInfo().getReasonPhrase());
		}
	}
}

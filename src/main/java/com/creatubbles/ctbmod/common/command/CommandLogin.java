package com.creatubbles.ctbmod.common.command;

import javax.ws.rs.core.Response;

import lombok.SneakyThrows;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import com.creatubbles.api.CreatubblesAPI;
import com.creatubbles.api.request.auth.SignInRequest;
import com.creatubbles.api.response.auth.SignInResponse;
import com.creatubbles.repack.endercore.common.util.ChatUtil;

public class CommandLogin extends CommandBase {

	public static String accessToken;

	@Override
	public String getCommandName() {
		return "ctb-login";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		return "/ctb-login <email> <password>";
	}

	@Override
	@SneakyThrows
	public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
		SignInRequest req = new SignInRequest(p_71515_2_[0], p_71515_2_[1]);
		Response resp = req.execute();
		if (resp.getStatus() != 200) {
			ChatUtil.sendNoSpamClient("Login failed! Invalid email or password.");
		} else {
			SignInResponse access = CreatubblesAPI.GSON.fromJson(resp.readEntity(String.class), SignInResponse.class);
			ChatUtil.sendNoSpamClient("Access Token Received: " + access.access_token);
			accessToken = access.access_token;
		}
	}

}

package com.creatubbles.ctbmod.common.command;

import lombok.SneakyThrows;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import com.creatubbles.ctbmod.common.http.HttpRequestException;
import com.creatubbles.ctbmod.common.http.LoginRequest;
import com.creatubbles.ctbmod.common.http.Login;
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
		LoginRequest req = new LoginRequest(new Login(p_71515_2_[0], p_71515_2_[1]));
		try {
			req.post();
			if (req.failed()) {
				ChatUtil.sendNoSpamClient("Login failed! Invalid email or password.");
			} else {
				ChatUtil.sendNoSpamClient("Access Token Received: " + req.getSuccessfulResult().getAccessToken());
				accessToken = req.getSuccessfulResult().getAccessToken();
			}
		} catch (HttpRequestException e) {
			ChatUtil.sendNoSpamClient("HTTP Error: " + e.getMessage());
		}
	}

}

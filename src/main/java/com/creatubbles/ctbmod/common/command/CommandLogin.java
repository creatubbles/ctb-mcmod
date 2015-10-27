package com.creatubbles.ctbmod.common.command;

import lombok.SneakyThrows;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

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
        SignInResponse resp = new SignInRequest(p_71515_2_[0], p_71515_2_[1]).execute().getResponse();
        if (resp.access_token == null) {
            ChatUtil.sendNoSpamClient("Login failed! Invalid email or password.");
        } else {
            ChatUtil.sendNoSpamClient("Access Token Received: " + resp.access_token);
            accessToken = resp.access_token;
        }
    }
}

package com.creatubbles.ctbmod.common.command;

import lombok.SneakyThrows;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

import com.creatubbles.api.CreatubblesAPI;
import com.creatubbles.api.request.auth.OAuthAccessTokenRequest;
import com.creatubbles.api.response.auth.OAuthAccessTokenResponse;
import com.creatubbles.repack.endercore.common.util.ChatUtil;

public class CommandLogin extends ClientCommandBase {

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
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length < 2) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        CreatubblesAPI.setStagingMode(false);
        OAuthAccessTokenResponse resp = new OAuthAccessTokenRequest(args[0], args[1]).execute().getResponse();
        if (resp.access_token == null) {
            ChatUtil.sendNoSpamClient("Login failed! Invalid email or password.");
        } else {
            ChatUtil.sendNoSpamClient("Access Token Received: " + resp.access_token);
            accessToken = resp.access_token;
        }
    }
}

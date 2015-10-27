package com.creatubbles.ctbmod.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import com.creatubbles.api.request.creator.UsersCreatorsRequest;
import com.creatubbles.api.response.creator.UsersCreatorsResponse;
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
        UsersCreatorsResponse resp = new UsersCreatorsRequest("me", CommandLogin.accessToken).execute().getResponse();
        if (resp.creators != null) {
            p_71515_1_.addChatMessage(ChatUtil.wrap("Found creators: " + resp.creators));
        } else {
            ChatUtil.sendNoSpamClient("Error: " + resp.message  );
        }
    }
}

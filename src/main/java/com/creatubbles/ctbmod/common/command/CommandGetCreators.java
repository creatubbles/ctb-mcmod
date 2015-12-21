package com.creatubbles.ctbmod.common.command;

import com.creatubbles.api.request.creator.GetCreatorsRequest;
import com.creatubbles.api.request.user.UserProfileRequest;
import com.creatubbles.api.response.creator.GetCreatorsResponse;
import com.creatubbles.api.response.user.UserProfileResponse;
import net.minecraft.command.ICommandSender;

import com.creatubbles.repack.endercore.common.util.ChatUtil;

public class CommandGetCreators extends ClientCommandBase {

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
        UserProfileResponse uPResponse = new UserProfileRequest(CommandLogin.accessToken).execute().getResponse();
        GetCreatorsResponse resp = new GetCreatorsRequest(uPResponse.user.id, CommandLogin.accessToken).
                execute().getResponse();
        if (resp.creators != null) {
            p_71515_1_.addChatMessage(ChatUtil.wrap("Found creators: " + resp.creators));
        } else {
            ChatUtil.sendNoSpamClient("Error: " + resp.message);
        }
    }
}

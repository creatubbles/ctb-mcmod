package com.creatubbles.ctbmod.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import com.creatubbles.ctbmod.common.http.HttpPostException;
import com.creatubbles.ctbmod.common.http.LoginRequest;
import com.creatubbles.ctbmod.common.http.User;
import com.enderio.core.common.util.ChatUtil;

public class CommandLogin extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "ctb-login";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_)
    {
        return "/ctb-login <email> <password>";
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_)
    {
        LoginRequest req = new LoginRequest(new User(p_71515_2_[0], p_71515_2_[1]));
        try
        {
            ChatUtil.sendNoSpamClient(req.post().toString());
        }
        catch (HttpPostException e)
        {
            e.printStackTrace();
            if (e.getResponse() != null)
            {
                System.out.println(e.getResponse().getEntity());
            }
        }
    }

}

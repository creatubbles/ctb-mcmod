package com.creatubbles.ctbmod.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import scala.actors.threadpool.Arrays;

import com.creatubbles.ctbmod.common.http.CreatorsRequest;
import com.creatubbles.ctbmod.common.http.CreatorsRequest.CreatorsResponse;
import com.creatubbles.ctbmod.common.http.HttpRequestException;
import com.creatubbles.repack.endercore.common.util.ChatUtil;

public class CommandGetCreators extends CommandBase
{

    @Override
    public String getCommandName()
    {
        return "ctb-creators";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_)
    {
        return "/ctb-creators";
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_)
    {
        CreatorsRequest req = new CreatorsRequest();
        try
        {
            req.post();
            if (!req.failed())
            {
                CreatorsResponse resp = req.getSuccessfulResult();
                p_71515_1_.addChatMessage(ChatUtil.wrap("Found creators: " + Arrays.toString(resp.getCreators())));
            }
            else
            {
                ChatUtil.sendNoSpamClient("Error: " + req.getFailedResult());
            }
        }
        catch (HttpRequestException e)
        {
            e.printStackTrace();
        }
    }

}

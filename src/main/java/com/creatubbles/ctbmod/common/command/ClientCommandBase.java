package com.creatubbles.ctbmod.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public abstract class ClientCommandBase extends CommandBase {

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender player) {
        return player.getEntityWorld().isRemote || super.canCommandSenderUseCommand(player);
    }
}

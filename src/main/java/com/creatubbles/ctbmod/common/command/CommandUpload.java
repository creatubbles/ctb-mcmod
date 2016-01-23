package com.creatubbles.ctbmod.common.command;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;

import com.creatubbles.api.request.amazon.UploadS3ImageRequest;
import com.creatubbles.api.request.creation.CreateCreationRequest;
import com.creatubbles.api.request.creation.CreationsUploadsRequest;
import com.creatubbles.api.request.creation.PingCreationsUploadsRequest;
import com.creatubbles.api.response.amazon.UploadS3ImageResponse;
import com.creatubbles.api.response.creation.CreateCreationResponse;
import com.creatubbles.api.response.creation.CreationsUploadsResponse;

import com.creatubbles.repack.endercore.common.util.ChatUtil;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import org.apache.commons.io.filefilter.FileFilterUtils;

public class CommandUpload extends ClientCommandBase {

    public static final String URL_BASE = "https://www.creatubbles.com/creations/";
    
    @Override
    public String getCommandName() {
        return "ctb-upload";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/ctb-upload <name> [screenshot number]";
    }

    @Override
    @SneakyThrows
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException("Not enough arguments. Usage: %s", getCommandUsage(sender));
        }

        if (CommandLogin.accessToken == null) {
            throw new WrongUsageException("/ctb-login must be run before this command can be used");
        }

        // create creation
        CreateCreationRequest createCreation = new CreateCreationRequest(CommandLogin.accessToken);
        createCreation.setData("{\"name\":\"" + args[0] + "\"}");
        CreateCreationResponse createCreationResponse = createCreation.execute().getResponse();
        
        String creationID = createCreationResponse.creation.id;

        // create url for upload
        CreationsUploadsRequest creationsUploads = new CreationsUploadsRequest(createCreationResponse.creation.id, CommandLogin.accessToken);
        CreationsUploadsResponse creationsUploadsResponse = creationsUploads.execute().getResponse();

        File screenshotsFolder = new File(Minecraft.getMinecraft().mcDataDir, "screenshots");
        File[] screenshots = screenshotsFolder.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".png"));

        int id = 0;
        try {
            id = Integer.valueOf(args[args.length - 1]);
        } catch (Exception e) {}
        
        if (id >= screenshots.length) {
            throw new NumberInvalidException("ID %d is too high! You only have %d screenshots, so ID must be between 0 and %d", id, screenshots.length, screenshots.length - 1);
        }
        sortScreenshots(screenshots);

        byte[] data = Files.readAllBytes(screenshots[id].toPath());

        // upload image to s3
        UploadS3ImageRequest uploadS3Image = new UploadS3ImageRequest(data, creationsUploadsResponse.url);
        UploadS3ImageResponse uploadS3Response = uploadS3Image.execute().getResponse();
        if (!uploadS3Response.success) {
            ChatUtil.sendNoSpamClient("Upload failed!");
            return;
        }
        PingCreationsUploadsRequest pingCreationsUploads = new PingCreationsUploadsRequest(creationsUploadsResponse.id, CommandLogin.accessToken);
        pingCreationsUploads.setData(""); // fixes null PUT error
        pingCreationsUploads.execute();
        
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN.toString().concat("[Creation upload successful! (Click to view)]")).setChatStyle(new ChatStyle()
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, URL_BASE + creationID))
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to view creation on website.")))));
    }

    private void sortScreenshots(File[] screenshots) {
        Arrays.sort(screenshots, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });
    }
}

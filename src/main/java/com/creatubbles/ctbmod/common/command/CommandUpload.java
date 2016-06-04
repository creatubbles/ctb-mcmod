package com.creatubbles.ctbmod.common.command;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import com.creatubbles.api.request.amazon.UploadS3FileRequest;
import com.creatubbles.api.request.creation.CreateCreationRequest;
import com.creatubbles.api.request.creation.CreationsUploadsRequest;
import com.creatubbles.api.request.creation.PingCreationsUploadsRequest;
import com.creatubbles.api.response.amazon.UploadS3FileResponse;
import com.creatubbles.api.response.creation.CreateCreationResponse;
import com.creatubbles.api.response.creation.CreationsUploadsResponse;
import com.creatubbles.repack.endercore.common.util.ChatUtil;
import com.google.common.base.Joiner;

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
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length < 1) {
            throw new WrongUsageException("Not enough arguments. Usage: %s", getCommandUsage(sender));
        }
        
        if (CommandLogin.accessToken == null) {
            throw new WrongUsageException("/ctb-login must be run before this command can be used");
        }
        // create creation
        CreateCreationRequest createCreation = new CreateCreationRequest(CommandLogin.accessToken);

        String name = args[0];
        try {
            name = getCreationName(args);
        } catch (Exception e) {
        }

        createCreation.setData("{\"name\":\"" + name + "\"}");
        CreateCreationResponse createCreationResponse = createCreation.execute().getResponse();

        String creationID = createCreationResponse.getId();

        File screenshotsFolder = new File(Minecraft.getMinecraft().mcDataDir, "screenshots");
        File[] screenshots = screenshotsFolder.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".png"));

        int id = 0;
        try {
            id = getScreenshotId(args);
        } catch (Exception e) {}

        if (id >= screenshots.length) {
            throw new NumberInvalidException("ID %d is too high! You only have %d screenshots, so ID must be between 0 and %d", id, screenshots.length, screenshots.length - 1);
        }
        sortScreenshots(screenshots);

        // create url for upload
        CreationsUploadsRequest creationsUploads = new CreationsUploadsRequest(createCreationResponse.getId(), FilenameUtils.getExtension(screenshots[id].getName()), CommandLogin.accessToken);
        CreationsUploadsResponse creationsUploadsResponse = creationsUploads.execute().getResponse();

        byte[] data = Files.readAllBytes(screenshots[id].toPath());

        // upload image to s3
        UploadS3FileRequest uploadS3Image = new UploadS3FileRequest(data, creationsUploadsResponse.getUrl(), creationsUploadsResponse.getType());
        UploadS3FileResponse uploadS3Response = uploadS3Image.execute().getResponse();
        if (!uploadS3Response.isSuccess()) {
            ChatUtil.sendNoSpamClient("Upload failed: " + uploadS3Response.getMessage());
            return;
        }
        
        PingCreationsUploadsRequest pingCreationsUploads = new PingCreationsUploadsRequest(creationsUploadsResponse.getPingUrl(), CommandLogin.accessToken);
        pingCreationsUploads.setData(""); // fixes null PUT error
        pingCreationsUploads.execute();

        sender.addChatMessage(new TextComponentString(TextFormatting.GREEN.toString().concat("[Creation upload successful! (Click to view)]")).setStyle(new Style()
                .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, URL_BASE + creationID))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to view creation on website.")))));
    }

    private int getScreenshotId(String[] args) {
        try {
            return Integer.parseInt(args[args.length - 1]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getCreationName(String[] args) {
        String s = Joiner.on(' ').join(args);
        // If no quotes, assume one-word name
        return s.contains("\"") ? s.substring(s.indexOf('"') + 1, s.lastIndexOf('"')) : args[1];
    }

    private void sortScreenshots(File[] screenshots) {
        Arrays.sort(screenshots, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });
    }
}

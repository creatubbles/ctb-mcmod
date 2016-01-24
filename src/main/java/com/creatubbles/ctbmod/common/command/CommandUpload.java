package com.creatubbles.ctbmod.common.command;

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
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;

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

        String name = args[0];
        try {
            name = getCreationName(args);
        } catch (Exception e) {
        }

        createCreation.setData("{\"name\":\"" + name + "\"}");
        CreateCreationResponse createCreationResponse = createCreation.execute().getResponse();

        String creationID = createCreationResponse.creation.id;

        // create url for upload
        CreationsUploadsRequest creationsUploads = new CreationsUploadsRequest(createCreationResponse.creation.id, CommandLogin.accessToken);
        CreationsUploadsResponse creationsUploadsResponse = creationsUploads.execute().getResponse();

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

    private int getScreenshotId(String[] args) {
        if (args.length == 1) {
            return 0;
        } else if (args[args.length - 1].endsWith("\"")) {
            return 0;
        } else if (args.length > 2 && args[args.length - 2].endsWith("\"") || args.length == 2) {
            return Integer.valueOf(args[args.length - 1]);
        }
        return 0;
    }

    private String getCreationName(String[] args) {
        String name;
        if (args.length == 1) {
            if (args[0].startsWith("\"") && args[0].endsWith("\"") && args[0].length() > 2) {
                name = args[0].substring(1, args[0].length() - 1);
            } else {
                name = args[0];
            }
        } else {
            if (args[0].startsWith("\"") && args[args.length - 1].endsWith("\"")) {
                StringBuilder sb = new StringBuilder();
                sb.append(args[0].substring(1));
                sb.append(StringUtils.SPACE);
                if (args.length > 2) {
                    sb.append(StringUtils.join(Arrays.copyOfRange(args, 1, args.length - 1), StringUtils.SPACE));
                    sb.append(StringUtils.SPACE);
                }
                sb.append(args[args.length - 1].substring(0, args[args.length - 1].length() - 1));
                name = sb.toString();
            } else if (args[0].startsWith("\"") && args.length > 2 && args[args.length - 2].endsWith("\"")) {
                StringBuilder sb = new StringBuilder();
                sb.append(args[0].substring(1));
                sb.append(StringUtils.SPACE);
                if (args.length > 3) {
                    sb.append(StringUtils.join(Arrays.copyOfRange(args, 1, args.length - 2), StringUtils.SPACE));
                    sb.append(StringUtils.SPACE);
                }
                sb.append(args[args.length - 2].substring(0, args[args.length - 2].length() - 1));
                name = sb.toString();
            } else {
                name = args[0];
            }
        }
        return name;
    }

    private void sortScreenshots(File[] screenshots) {
        Arrays.sort(screenshots, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });
    }
}

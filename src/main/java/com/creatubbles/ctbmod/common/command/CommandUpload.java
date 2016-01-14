package com.creatubbles.ctbmod.common.command;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;

import com.creatubbles.api.request.amazon.UploadS3ImageRequest;
import com.creatubbles.api.request.creation.CreateCreationRequest;
import com.creatubbles.api.request.creation.CreationsUploadsRequest;
import com.creatubbles.api.request.creation.PingCreationsUploadsRequest;
import com.creatubbles.api.response.creation.CreateCreationResponse;
import com.creatubbles.api.response.creation.CreationsUploadsResponse;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.ArrayUtils;

public class CommandUpload extends ClientCommandBase {

    @Override
    public String getCommandName() {
        return "ctb-upload";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/ctb-upload <name> <screenshot number>";
    }

    @Override
    @SneakyThrows
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new WrongUsageException("Not enough arguments. Usage: %s", getCommandUsage(sender));
        }
        
        if (CommandLogin.accessToken == null) {
            throw new WrongUsageException("/ctb-login must be run before this command can be used");
        }
        // create creation
        CreateCreationRequest createCreation = new CreateCreationRequest(CommandLogin.accessToken);
        createCreation.setData("{\"name\":\"" + args[0] + "\"}");
        CreateCreationResponse createCreationResponse = createCreation.execute().getResponse();
        System.out.println(createCreationResponse.creation.id);

        // create url for upload
        CreationsUploadsRequest creationsUploads = new CreationsUploadsRequest(createCreationResponse.creation.id, CommandLogin.accessToken);
        CreationsUploadsResponse creationsUploadsResponse = creationsUploads.execute().getResponse();
        System.out.println(creationsUploadsResponse.url);

        File screenshotsFolder = new File(Minecraft.getMinecraft().mcDataDir, "screenshots");
        File[] screenshots = screenshotsFolder.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".png"));
        ArrayUtils.reverse(screenshots);

        int id;
        try {
            id = Integer.valueOf(args[args.length - 1]);
        } catch (Exception e) {
            throw new NumberInvalidException("%s is not a valid ID number. Must be a number from 0 to %d", args[args.length - 1], screenshots.length - 1);
        }

        byte[] data = Files.readAllBytes(screenshots[id].toPath());

        // upload image to s3
        UploadS3ImageRequest uploadS3Image = new UploadS3ImageRequest(data, creationsUploadsResponse.url);
        uploadS3Image.execute().getResponse();

        PingCreationsUploadsRequest pingCreationsUploads = new PingCreationsUploadsRequest(creationsUploadsResponse.id, CommandLogin.accessToken);
        pingCreationsUploads.setData(""); // fixes null PUT error
        System.out.println(pingCreationsUploads.execute().getResponse().message);
    }
}

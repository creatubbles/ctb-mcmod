package com.creatubbles.ctbmod.common.command;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.creatubbles.api.core.Creation;
import com.creatubbles.api.core.Credentials;
import com.creatubbles.api.request.amazon.GetAmazonTokenRequest;
import com.creatubbles.api.request.amazon.UploadS3ImageRequest;
import com.creatubbles.api.request.creation.UpdateCreationRequest;
import com.creatubbles.api.request.creation.UploadCreationRequest;
import com.creatubbles.api.response.amazon.GetAmazonTokenResponse;
import com.creatubbles.api.response.creation.UploadCreationResponse;

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
        UploadCreationResponse uploadResponse = new UploadCreationRequest(CommandLogin.accessToken).execute().getResponse();
        // get required info for s3
        GetAmazonTokenResponse amazonTokenResponse = new GetAmazonTokenRequest(CommandLogin.accessToken).execute().getResponse();
        Credentials credentials = amazonTokenResponse.credentials;

        File screenshotsFolder = new File(Minecraft.getMinecraft().mcDataDir, "screenshots");
        File[] screenshots = screenshotsFolder.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".png"));
        ArrayUtils.reverse(screenshots);

        int id;
        try {
            id = Integer.valueOf(args[args.length - 1]);
        } catch (Exception e) {
            throw new NumberInvalidException("%s is not a valid ID number. Must be a number from 0 to %d", args[args.length - 1], screenshots.length - 1);
        }

        args = ArrayUtils.subarray(args, 0, args.length - 1);

        byte[] data = Files.readAllBytes(screenshots[id].toPath());
        String fileName = System.currentTimeMillis() + "creation.png";
        Creation creation = uploadResponse.creation;
        String relativePath = creation.store_dir + "/" + fileName;

        // upload image
        new UploadS3ImageRequest(data, relativePath, credentials.access_key_id, credentials.secret_access_key, credentials.session_token).execute().getResponse();
        creation.url = relativePath;
        creation.name = StringUtils.join(args, " ");
        // update creation(url)
        new UpdateCreationRequest(CommandLogin.accessToken, creation).execute().getResponse();
    }
}

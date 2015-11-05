package com.creatubbles.ctbmod.common.command;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.ArrayUtils;

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
        
        // create creation
        UploadCreationResponse uploadResponse = new UploadCreationRequest(CommandLogin.accessToken).execute().getResponse();
        // get required info for s3
        GetAmazonTokenResponse amazonTokenResponse = new GetAmazonTokenRequest(CommandLogin.accessToken).execute().getResponse();
        Credentials credentials = amazonTokenResponse.credentials;

        File screenshotsFolder = new File(Minecraft.getMinecraft().mcDataDir, "screenshots");
        File[] screenshots = screenshotsFolder.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".png"));
        ArrayUtils.reverse(screenshots);
        byte[] data = Files.readAllBytes(screenshots[Integer.valueOf(args[1])].toPath());
        String fileName = System.currentTimeMillis() + "creation.png";
        Creation creation = uploadResponse.creation;
        String relativePath = creation.store_dir + "/" + fileName;
       
        // upload image
        new UploadS3ImageRequest(data, relativePath, credentials.access_key_id, credentials.secret_access_key, credentials.session_token).execute().getResponse();
        creation.url = relativePath;
        creation.name = args[0];
        // update creation(url)
        new UpdateCreationRequest(CommandLogin.accessToken, creation).execute().getResponse();
    }
}

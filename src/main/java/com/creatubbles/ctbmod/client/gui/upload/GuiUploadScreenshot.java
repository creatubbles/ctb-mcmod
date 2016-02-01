package com.creatubbles.ctbmod.client.gui.upload;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.io.FileUtils;
import org.lwjgl.opengl.GL11;

import com.creatubbles.api.request.amazon.UploadS3ImageRequest;
import com.creatubbles.api.request.creation.CreateCreationRequest;
import com.creatubbles.api.request.creation.CreationsUploadsRequest;
import com.creatubbles.api.request.creation.PingCreationsUploadsRequest;
import com.creatubbles.api.response.amazon.UploadS3ImageResponse;
import com.creatubbles.api.response.creation.CreateCreationResponse;
import com.creatubbles.api.response.creation.CreationsUploadsResponse;
import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.client.gui.GuiUtil;
import com.creatubbles.ctbmod.client.gui.LazyLoadedTexture;
import com.creatubbles.ctbmod.common.command.CommandLogin;
import com.creatubbles.repack.endercore.client.gui.GuiContainerBase;
import com.creatubbles.repack.endercore.client.gui.widget.TextFieldEnder;
import com.creatubbles.repack.endercore.common.util.ChatUtil;
import com.google.common.util.concurrent.ListenableFuture;

public class GuiUploadScreenshot extends GuiContainerBase {

    private static final ResourceLocation SCREENSHOT_RES = new ResourceLocation(CTBMod.DOMAIN, "screenshotprev");
    private static final String URL_BASE = "https://www.creatubbles.com/creations/";

    private final GuiScreenshotList parent;
    private final File file;

    private Thread thread;
    private ListenableFuture<?> uploadTask;
    private LazyLoadedTexture tex;
    private Dimension size;
    
    private GuiButton buttonUpload, buttonBack;

    private final TextFieldEnder tfName;

    @SneakyThrows
    public GuiUploadScreenshot(GuiScreenshotList parent, File file) {
        super(GuiUtil.dummyContainer());
        this.parent = parent;
        this.file = file;

        thread = new Thread(new Runnable() {

            @Override
            @SneakyThrows
            public void run() {
                BufferedImage original = ImageIO.read(GuiUploadScreenshot.this.file);
                size = new Dimension(original.getWidth(), original.getHeight());
                tex = new LazyLoadedTexture(GuiUtil.upsize(ImageIO.read(GuiUploadScreenshot.this.file), false));
                uploadTask = Minecraft.getMinecraft().addScheduledTask(new Runnable() {

                    @Override
                    public void run() {
                        tex.uploadTexture();
                        Minecraft.getMinecraft().getTextureManager().loadTexture(SCREENSHOT_RES, tex);
                    }
                });
            }
        });
        thread.start();

        tfName = new TextFieldEnder(Minecraft.getMinecraft().fontRendererObj, 20, 0, 300, 20);
        tfName.setFocused(true);
        textFields.add(tfName);
    }

    @Override
    public void initGui() {
        this.xSize = width;
        this.ySize = height;

        tfName.setYOrigin(height - 30);
        tfName.width = width / 2 - tfName.xPosition;

        super.initGui();

        int x = (width / 2) + (width / 4);
        int w = width / 5;
        addButton(buttonUpload = new GuiButton(0, x - (w / 10) - w, height - 30, w, 20, "Upload!"));
        addButton(buttonBack = new GuiButton(-1, x + (w / 10), height - 30, w, 20, "Back"));
    }

    @Override
    public void onGuiClosed() {
        GL11.glDeleteTextures(tex.getGlTextureId());
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY) {
        this.drawBackground(0);
        buttonUpload.enabled = !tfName.getText().isEmpty();
        super.drawGuiContainerBackgroundLayer(par1, mouseX, mouseY);
        GuiUtil.drawSlotBackground(0, 20, width, height - 60);
    }

    @Override
    protected void drawForegroundImpl(int mouseX, int mouseY) {
        super.drawForegroundImpl(mouseX, mouseY);

        drawCenteredString(fontRendererObj, file.getName(), width / 2, 10 - (fontRendererObj.FONT_HEIGHT / 2), 0xFFFFFF);

        if (uploadTask != null && uploadTask.isDone()) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(SCREENSHOT_RES);
            GuiUtil.drawRectInscribed(new Rectangle(size), new Rectangle(50, 40, width - 100, height - 100), tex.getWidth(), tex.getHeight());
        } else {
            GlStateManager.enableBlend();
            GuiUtil.drawLoadingTex(width / 2 - 32, height / 2 - 25 - 32, 64, 64);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button.id < 0) {
            Minecraft.getMinecraft().displayGuiScreen(parent);
        } else {

            final String accessToken = CTBMod.cache.getOAuth().getAccessToken();
            final String name = tfName.getText();

            tfName.setText("");
            
            buttonBack.enabled = false;
            
            new Thread(new Runnable() {
                
                @Override
                public void run() {

                    Exception e = null;
                    String creationID = null;
                    
                    try {
                        // create creation
                        CreateCreationRequest createCreation = new CreateCreationRequest(accessToken);

                        createCreation.setData("{\"name\":\"" + name + "\"}");
                        
                        buttonUpload.displayString = "Creating...";
                        CreateCreationResponse createCreationResponse = createCreation.execute().getResponse();

                        creationID = createCreationResponse.creation.id;

                        // create url for upload
                        buttonUpload.displayString = "Uploading...";
                        CreationsUploadsRequest creationsUploads = new CreationsUploadsRequest(createCreationResponse.creation.id, accessToken);
                        CreationsUploadsResponse creationsUploadsResponse = creationsUploads.execute().getResponse();

                        byte[] data = FileUtils.readFileToByteArray(file);

                        // upload image to s3
                        UploadS3ImageRequest uploadS3Image = new UploadS3ImageRequest(data, creationsUploadsResponse.url);
                        UploadS3ImageResponse uploadS3Response = uploadS3Image.execute().getResponse();

                        if (!uploadS3Response.success) {
                            throw new RuntimeException("Upload Failed: " + uploadS3Response.message);
                        }

                        buttonUpload.displayString = "Finalizing...";
                        PingCreationsUploadsRequest pingCreationsUploads = new PingCreationsUploadsRequest(creationsUploadsResponse.id, CommandLogin.accessToken);
                        pingCreationsUploads.setData(""); // fixes null PUT error
                        pingCreationsUploads.execute();
                    } catch (Exception e2) {
                        e = e2;
                    }
                    
                    final Exception error = e;
                    final String creation = creationID;
                    
                    Minecraft.getMinecraft().addScheduledTask(new Runnable() {

                        @Override
                        public void run() {

                            if (error == null) {
                                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN.toString().concat("[Creation upload successful! (Click to view \"" + name + "\")]")).setChatStyle(new ChatStyle().setChatClickEvent(
                                        new ClickEvent(ClickEvent.Action.OPEN_URL, URL_BASE + creation)).setChatHoverEvent(
                                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to view creation on website.")))));

                            } else {
                                ChatUtil.sendNoSpamClient("Upload failed: " + error.getMessage());
                                error.printStackTrace();
                                return;
                            }
                            
                            Minecraft.getMinecraft().displayGuiScreen(null);
                        }
                    });
                }
            }).start();
          
        }
    }
}

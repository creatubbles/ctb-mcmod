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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import com.creatubbles.api.request.amazon.UploadS3FileRequest;
import com.creatubbles.api.request.creation.CreateCreationRequest;
import com.creatubbles.api.request.creation.CreationsUploadsRequest;
import com.creatubbles.api.request.creation.PingCreationsUploadsRequest;
import com.creatubbles.api.request.landingurls.GetCreationLandingUrlRequest;
import com.creatubbles.api.response.amazon.UploadS3FileResponse;
import com.creatubbles.api.response.creation.CreateCreationResponse;
import com.creatubbles.api.response.creation.CreationsUploadsResponse;
import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.client.gui.GuiUtil;
import com.creatubbles.ctbmod.client.gui.LazyLoadedTexture;
import com.creatubbles.repack.endercore.client.gui.GuiContainerBase;
import com.creatubbles.repack.endercore.client.gui.button.IconButton;
import com.creatubbles.repack.endercore.client.gui.widget.TextFieldEnder;
import com.creatubbles.repack.endercore.client.render.EnderWidget;
import com.creatubbles.repack.endercore.common.util.ChatUtil;
import com.google.common.util.concurrent.ListenableFuture;

public class GuiUploadScreenshot extends GuiContainerBase {

    private static final ResourceLocation SCREENSHOT_RES = new ResourceLocation(CTBMod.DOMAIN, "screenshotprev");

    private final GuiScreenshotList parent;
    private final File[] files;
    private final int index;

    private Thread thread;
    private ListenableFuture<?> uploadTask;
    private LazyLoadedTexture tex;
    private Dimension size;
    
    private GuiButton buttonUpload, buttonBack, buttonNext, buttonPrev;
    
    private IconButton buttonDelete;
    private int confirm;
    private boolean parentInvalid;

    private final TextFieldEnder tfName;

    private GuiUploadScreenshot(GuiUploadScreenshot old, File[] files, int index) {
        this(old.parent, files, index);
        this.parentInvalid = old.parentInvalid;
    }
    
    @SneakyThrows
    public GuiUploadScreenshot(GuiScreenshotList parent, final File[] files, final int index) {
        super(GuiUtil.dummyContainer());
        this.parent = parent;
        this.files = files;
        this.index = index;

        thread = new Thread(new Runnable() {

            @Override
            @SneakyThrows
            public void run() {
                BufferedImage original = ImageIO.read(files[index]);
                size = new Dimension(original.getWidth(), original.getHeight());
                tex = new LazyLoadedTexture(GuiUtil.upsize(ImageIO.read(files[index]), false));
                uploadTask = Minecraft.getMinecraft().addScheduledTask(new Runnable() {

                    @Override
                    public void run() {
                        if (Minecraft.getMinecraft().currentScreen == GuiUploadScreenshot.this) {
                            tex.uploadTexture();
                            Minecraft.getMinecraft().getTextureManager().loadTexture(SCREENSHOT_RES, tex);
                        }
                    }
                });
            }
        });
        thread.start();

        tfName = new TextFieldEnder(Minecraft.getMinecraft().fontRendererObj, 20, 0, 300, 20);
        tfName.setFocused(true);
        textFields.add(tfName);
        
        buttonDelete = new IconButton(this, -98, 0, 2, EnderWidget.CROSS);
        buttonDelete.setToolTip("Delete");
    }

    @Override
    public void initGui() {
        this.xSize = width;
        this.ySize = height;

        tfName.setYOrigin(height - 30);
        tfName.width = width / 2 - tfName.xPosition;
        
        buttonDelete.setXOrigin((width / 2) + (fontRendererObj.getStringWidth(files[index].getName()) / 2) - 14);
        buttonDelete.getToolTip().setBounds(buttonDelete.getBounds());

        super.initGui();
        
        buttonDelete.onGuiInit();

        int x = (width / 2) + (width / 4);
        int w = width / 5;
        addButton(buttonUpload = new GuiButton(0, x - (w / 10) - w, height - 30, w, 20, "Upload!"));
        addButton(buttonBack = new GuiButton(-99, x + (w / 10), height - 30, w, 20, "Back"));
        
        x = width / 2;
        w = (width - 150) / 4;
        addButton(buttonPrev = new GuiButton(-1, x - w - 10, height - 75, w, 20, "<< Prev"));
        addButton(buttonNext = new GuiButton(1, x + 10, height - 75, w, 20, "Next >> "));
    }

    @Override
    public void onGuiClosed() {
        if (tex != null) { // Someone is spamming next/prev
            GL11.glDeleteTextures(tex.getGlTextureId());
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (confirm > 0 && confirm < Integer.MAX_VALUE) {
            confirm++;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY) {
        this.drawBackground(0);
        buttonUpload.enabled = !tfName.getText().isEmpty();
        buttonNext.enabled = index != files.length - 1;
        buttonPrev.enabled = index != 0;

        if (confirm > 0) {
            buttonDelete.enabled = confirm > 20;
            if (confirm > 20) {
                buttonDelete.setIcon(EnderWidget.TICK);
            }
        }

        super.drawGuiContainerBackgroundLayer(par1, mouseX, mouseY);
        GuiUtil.drawSlotBackground(0, 20, width, height - 60);
    }

    @Override
    protected void drawForegroundImpl(int mouseX, int mouseY) {
        super.drawForegroundImpl(mouseX, mouseY);

        String name = files[index].getName();
        int x = width / 2 - 16;
        int y = 10 - (fontRendererObj.FONT_HEIGHT / 2);
        drawCenteredString(fontRendererObj, name, x, y, 0xFFFFFF);
        if (confirm > 0) {
            drawString(fontRendererObj, "Confirm?", x + (fontRendererObj.getStringWidth(name) / 2) + 16 + 4, y, 0xFFFFFF);
        }

        if (uploadTask != null && uploadTask.isDone()) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(SCREENSHOT_RES);
            GuiUtil.drawRectInscribed(new Rectangle(size), new Rectangle(50, 40, width - 100, height - 120), tex.getWidth(), tex.getHeight());
        } else {
            GlStateManager.enableBlend();
            GuiUtil.drawLoadingTex(width / 2 - 32, height / 2 - 25 - 32, 64, 64);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button.id == 0) {

            final String accessToken = CTBMod.cache.getOAuth().getAccessToken();
            final String name = tfName.getText();

            tfName.setText("");
            
            buttonBack.enabled = false;
            
            new Thread(new Runnable() {
                
                @Override
                public void run() {

                    Exception e = null;
                    String landingUrl = null;
                    
                    try {
                        // create creation
                        CreateCreationRequest createCreation = new CreateCreationRequest(accessToken);

                        createCreation.setData("{\"name\":\"" + name + "\"}");
                        
                        buttonUpload.displayString = "Creating...";
                        CreateCreationResponse createCreationResponse = createCreation.execute().getResponse();

                        // create url for upload
                        buttonUpload.displayString = "Uploading...";
                        CreationsUploadsRequest creationsUploads = new CreationsUploadsRequest(createCreationResponse.creation.id, FilenameUtils.getExtension(files[index].getName()), accessToken);
                        CreationsUploadsResponse creationsUploadsResponse = creationsUploads.execute().getResponse();

                        byte[] data = FileUtils.readFileToByteArray(files[index]);

                        // upload image to s3
                        UploadS3FileRequest uploadS3Image = new UploadS3FileRequest(data, creationsUploadsResponse.url, creationsUploadsResponse.content_type);
                        UploadS3FileResponse uploadS3Response = uploadS3Image.execute().getResponse();

                        if (!uploadS3Response.success) {
                            throw new RuntimeException("Upload Failed: " + uploadS3Response.message);
                        }

                        buttonUpload.displayString = "Finalizing...";
                        PingCreationsUploadsRequest pingCreationsUploads = new PingCreationsUploadsRequest(creationsUploadsResponse.ping_url, accessToken);
                        pingCreationsUploads.setData(""); // fixes null PUT error
                        pingCreationsUploads.execute();
                        
                        GetCreationLandingUrlRequest landingReq = new GetCreationLandingUrlRequest(createCreationResponse.creation.id, accessToken);
                        landingUrl = landingReq.execute().getResponse().url;
                        
                        
                    } catch (Exception e2) {
                        e = e2;
                    }
                    
                    final Exception error = e;
                    final String landingUrlf = landingUrl;
                    
                    Minecraft.getMinecraft().addScheduledTask(new Runnable() {

                        @Override
                        public void run() {

                            if (error == null) {
                                Minecraft.getMinecraft().thePlayer.addChatMessage(new TextComponentString(TextFormatting.GREEN.toString().concat("[Creation upload successful! (Click to view \"" + name + "\")]")).setStyle(new Style().setClickEvent(
                                        new ClickEvent(ClickEvent.Action.OPEN_URL, landingUrlf)).setHoverEvent(
                                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to view creation on website.")))));

                            } else {
                                ChatUtil.sendNoSpamClient("Upload failed: " + error.getMessage());
                                error.printStackTrace();
                            }
                            
                            Minecraft.getMinecraft().displayGuiScreen(null);
                        }
                    });
                }
            }).start();

        } else if (button.id == -99) {
            Minecraft.getMinecraft().displayGuiScreen(parentInvalid ? new GuiScreenshotList(parent.parent) : parent);
        } else if (button.id == -98) {
            if (confirm > 20) {
                files[index].delete();
                File[] newFiles = ArrayUtils.remove(files, index);
                parentInvalid = true;
                Minecraft.getMinecraft().displayGuiScreen(new GuiUploadScreenshot(this, newFiles, index));
            } else {
                confirm++;
            }
        } else {
            int newIndex = index + button.id;
            if (newIndex >= 0 && newIndex < files.length) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiUploadScreenshot(this, files, newIndex));
            }
        }
    }
}

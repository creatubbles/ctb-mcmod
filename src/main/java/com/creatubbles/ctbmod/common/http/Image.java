package com.creatubbles.ctbmod.common.http;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;

import com.creatubbles.ctbmod.CTBMod;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@RequiredArgsConstructor
public class Image
{
    private String url;

    @NonFinal
    private transient BufferedImage image;

    public boolean downloaded()
    {
        return image != null;
    }

    public void download()
    {
        // Spawn download thread so as to not pause the main thread
        // Users of this class should be checking the downloaded() method to see if this has finished
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    image = ImageIO.read(new URL(url));
                }
                catch (IOException e)
                {
                    LogManager.getLogger(CTBMod.MODID).error("Could not download image from " + url);
                    e.printStackTrace();
                }
                finally
                {
                    // This is a recreation of the "no texture" texture
                    image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
                    image.setRGB(0, 0, 0xFF00FF);
                    image.setRGB(1, 1, 0xFF00FF);
                }
            }
        }).start();
    }
}

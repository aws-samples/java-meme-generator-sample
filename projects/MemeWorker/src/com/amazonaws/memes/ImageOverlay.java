/*
 * Copyright 2012-2013 Amazon Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amazonaws.memes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Worker that knows how to overlay text onto an image.
 */
public class ImageOverlay {

    private static final int MAX_FONT_SIZE = 48;
    private static final int BOTTOM_MARGIN = 10;
    private static final int TOP_MARGIN = 5;
    private static final int SIDE_MARGIN = 10;
    private static final String TOP_TEXT = "This is a long bit of text on the top, but don't worry, this algorithm will take care of it";
    // private static final String BOTTOM_TEXT =
    // "Some long text is too long for one line, and if we continue it gets really long indeed";
    private static final String BOTTOM_TEXT = "NOPE";

    private static final String INPUT_IMAGE = "/tmp/CHANGEME";
    private static final String CAPTION_FILE = "/tmp/CHANGEME.out";

    public static BufferedImage overlay(BufferedImage image, String topCaption, String bottomCaption)
            throws IOException, InterruptedException {
        Graphics graphics = image.getGraphics();
        drawStringCentered(graphics, topCaption, image, true);
        drawStringCentered(graphics, bottomCaption, image, false);
        return image;
    }

    public static void main(String[] args) throws InterruptedException {

        try {
            if ( new File(CAPTION_FILE).exists() ) {
                new File(CAPTION_FILE).delete();
            }
            BufferedImage image = ImageIO.read(new File(INPUT_IMAGE));
            Graphics graphics = image.getGraphics();
            String captionTop = TOP_TEXT;
            String captionBottom = BOTTOM_TEXT;
            drawStringCentered(graphics, captionTop, image, true);
            drawStringCentered(graphics, captionBottom, image, false);

            ImageIO.write(image, "png", new File(CAPTION_FILE));
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Draws the given string centered, as big as possible, on either the top or
     * bottom 20% of the image given.
     */
    private static void drawStringCentered(Graphics g, String text, BufferedImage image, boolean top) throws InterruptedException {
        if (text == null)
            text = "";

        int height = 0;
        int fontSize = MAX_FONT_SIZE;
        int maxCaptionHeight = image.getHeight() / 5;
        int maxLineWidth = image.getWidth() - SIDE_MARGIN * 2;
        String formattedString = "";

        do {
            g.setFont(new Font("Arial", Font.BOLD, fontSize));

            // first inject newlines into the text to wrap properly
            StringBuilder sb = new StringBuilder();
            int left = 0;
            int right = text.length() - 1;
            while ( left < right ) {

                String substring = text.substring(left, right + 1);
                Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(substring, g);
                while ( stringBounds.getWidth() > maxLineWidth ) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }

                    // look for a space to break the line
                    boolean spaceFound = false;
                    for ( int i = right; i > left; i-- ) {
                        if ( text.charAt(i) == ' ' ) {
                            right = i - 1;
                            spaceFound = true;
                            break;
                        }
                    }
                    substring = text.substring(left, right + 1);
                    stringBounds = g.getFontMetrics().getStringBounds(substring, g);

                    // If we're down to a single word and we are still too wide,
                    // the font is just too big.
                    if ( !spaceFound && stringBounds.getWidth() > maxLineWidth ) {
                        break;
                    }
                }
                sb.append(substring).append("\n");
                left = right + 2;
                right = text.length() - 1;
            }

            formattedString = sb.toString();

            // now determine if this font size is too big for the allowed height
            height = 0;
            for ( String line : formattedString.split("\n") ) {
                Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(line, g);
                height += stringBounds.getHeight();
            }
            fontSize--;
        } while ( height > maxCaptionHeight );

        // draw the string one line at a time
        int y = 0;
        if ( top ) {
            y = TOP_MARGIN + g.getFontMetrics().getHeight();
        } else {
            y = image.getHeight() - height - BOTTOM_MARGIN + g.getFontMetrics().getHeight();
        }
        for ( String line : formattedString.split("\n") ) {
            // Draw each string twice for a shadow effect
            Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(line, g);
            g.setColor(Color.BLACK);
            g.drawString(line, (image.getWidth() - (int) stringBounds.getWidth()) / 2 + 2, y + 2);
            g.setColor(Color.WHITE);
            g.drawString(line, (image.getWidth() - (int) stringBounds.getWidth()) / 2, y);
            y += g.getFontMetrics().getHeight();
        }
    }
}

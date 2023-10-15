package com.example.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImageToPDF {

        public static void imageTodPdf(List<String> imagePaths) {
            try {
                // Create a new PDF document
                PDDocument document = new PDDocument();


                for (String imagePath : imagePaths) {
                    // Load the image
                    BufferedImage image = ImageIO.read(new File(imagePath));

                    // Create a page
                    PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
                    document.addPage(page);

                    // Create a content stream for the page
                    PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true);

                    // Create an XObject from the image
                    PDImageXObject xImage = LosslessFactory.createFromImage(document, image);

                    // Draw the image on the page
                    contentStream.drawImage(xImage, 0, 0);

                    // Close the content stream
                    contentStream.close();
                }
                // Save the document
                document.save("output.pdf");
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }





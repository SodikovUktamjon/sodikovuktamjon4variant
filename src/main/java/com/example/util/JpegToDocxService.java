package com.example.util;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ForkJoinPool;


public class JpegToDocxService {
    public static void convertJpegToDocx(List<String> imagePaths) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
//        forkJoinPool.execute(() -> {
            try {
                XWPFDocument document = new XWPFDocument();

                XWPFParagraph paragraph
                        = document.createParagraph();
                XWPFRun run = paragraph.createRun();

                FileOutputStream fout = new FileOutputStream(
                        "output.docx");

                for (String image1 : imagePaths) {
                    File image = new File(image1);
                    FileInputStream imageData
                            = new FileInputStream(image);

                    int imageType = XWPFDocument.PICTURE_TYPE_JPEG;
                    String imageFileName = image.getName();

                    int width = 450;
                    int height = 400;


                    run.addPicture(imageData, imageType, imageFileName,
                            Units.toEMU(width),
                            Units.toEMU(height));
                }
                    document.write(fout);

                    fout.close();
                    document.close();

            } catch (InvalidFormatException | IOException e) {
                throw new RuntimeException(e);
            }

//        });
    }

}

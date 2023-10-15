package com.example.util;


import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class TextExtractorWithPreprocessing {

    public static String extractTextFromImage(String imagePath) {

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("D:/Tesseract/tessdata");
        tesseract.setLanguage("eng+rus+ara+fas+fra+ita+spa+deu+por+chi_sim+chi_tra+jpn+kor+pol");

        try {
            String s = tesseract.doOCR(new File(imagePath));//agar preprocess qilinadiganda tempImagePath ni imagePath ga o'zgartirish kerak
            return s;
        } catch (TesseractException e) {
            e.printStackTrace();
            return "Error during OCR: " + e.getMessage();
        }
    }
}

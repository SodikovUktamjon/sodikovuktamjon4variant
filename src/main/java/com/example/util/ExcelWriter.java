package com.example.util;


import com.example.domains.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class ExcelWriter {
    public static File generateUsersExcelFile(List<User> users, String filePath)  {
        File excelFile = new File(filePath);


        new ForkJoinPool().execute(() -> {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Users");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);

            String[] headers = {"ID", "First Name", "Last Name", "Username", "Chat ID", "Role", "Learning Regime", "User Language"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getFirstName());
                row.createCell(2).setCellValue(user.getLastName());
                row.createCell(3).setCellValue(user.getUsername());
                row.createCell(4).setCellValue(user.getChatId());
                row.createCell(5).setCellValue(user.getRole().toString());
                row.createCell(6).setCellValue(user.getLang());
            }


            try (FileOutputStream outputStream = new FileOutputStream(excelFile)) {
                workbook.write(outputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
        return excelFile;
    }
}

package com.example.tsurratingbot2022.factories;

import com.example.tsurratingbot2022.Company;
import com.example.tsurratingbot2022.Criteria;
import com.example.tsurratingbot2022.RatingForm;
import com.example.tsurratingbot2022.repositories.CompanyRepo;
import com.example.tsurratingbot2022.repositories.CriteriaRepo;
import com.example.tsurratingbot2022.repositories.FormRepo;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Component
public class ExcelFileCreator {

    private final FormRepo formRepo;
    private final CriteriaRepo criteriaRepo;
    private final CompanyRepo companyRepo;

    public ExcelFileCreator(FormRepo formRepo, CriteriaRepo criteriaRepo, CompanyRepo companyRepo) {
        this.formRepo = formRepo;
        this.criteriaRepo = criteriaRepo;
        this.companyRepo = companyRepo;
    }

    public File createExcel(Calendar start, Calendar end) {
        File file = null;
        try {
            file = ResourceUtils.getFile("classpath:files/otchet.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Test");
            CreationHelper factory = workbook.getCreationHelper();
            Drawing drawing = sheet.createDrawingPatriarch();
            ClientAnchor anchor = factory.createClientAnchor();
            CellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            style.setAlignment(HorizontalAlignment.CENTER_SELECTION);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            XSSFRow firstRow = sheet.createRow(0);
            fillFirstRow(firstRow, style);
            int critLength = firstRow.getLastCellNum();
            int rowNum = 1;
            for (Company company : companyRepo.findAllByOrderById()) {
                int columnNumber = 0;
                long companyScore = 0;
                XSSFRow row = sheet.createRow(rowNum++);
                List<RatingForm> ratings = formRepo.findAllByCompanyAndScoreDateBetween(company.getName(), start.getTime(), end.getTime());
                row.createCell(columnNumber++).setCellValue(company.getCompanyType().getName());
                row.createCell(columnNumber++).setCellValue(company.getName());
                row.createCell(columnNumber++).setCellValue(new SimpleDateFormat("MMMMM", new Locale("ru")).format(start.getTime()));
                row.createCell(columnNumber++);
                for (int i = columnNumber; i < critLength; i++) {
                    String critName = firstRow.getCell(i).getStringCellValue();
                    List<RatingForm> critList = ratings.stream().filter(ratingForm -> ratingForm.getCriteria().equals(critName)).collect(Collectors.toList());
                    if (critList.size()==0) continue;
                    Cell cell = row.createCell(i);
                    long totalScore = 0;
                    String text = "";
                    for (RatingForm ratingForm : critList) {
                        long score = ratingForm.getScore();
                        totalScore+=score;
                        text+= score > 0 ? "+" : "";
                        text+=String.format("%d %s\n%s\n", score, ratingForm.getUserName(),ratingForm.getDescription());
                    }
                    cell.setCellValue(totalScore);
                    createCellWithDescription(cell, drawing, anchor, factory, text);
                    companyScore+=totalScore;
                }
                row.createCell(columnNumber-1).setCellValue(companyScore);
            }
            for (int i = 0; i < critLength+3; i++) {
                if (i < 4) sheet.autoSizeColumn(i);
                else sheet.setColumnWidth(i, 4000);
            }
           try (FileOutputStream stream = new FileOutputStream(file)) {
               workbook.write(stream);
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private void createCellWithDescription(Cell cell, Drawing drawing, ClientAnchor anchor, CreationHelper factory, String str) {
        anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_DO_RESIZE);
        str = str.strip();
        int commentHeight = Arrays.stream(str.split("\\n")).mapToInt(s -> s.length()/40).sum();
        String max = Arrays.stream(str.split("\\n")).max(Comparator.comparingInt(String::length)).get();
        int commentWidth = Math.min(max.length() / 13 + 1, 3);
        commentHeight += str.split("\\n").length;
        anchor.setCol1(cell.getColumnIndex()+1);
        anchor.setCol2(cell.getColumnIndex()+1+commentWidth);
        anchor.setRow1(cell.getRowIndex()+1);
        anchor.setRow2(cell.getRowIndex()+1+commentHeight);
        Comment comment = drawing.createCellComment(anchor);
        comment.setString(factory.createRichTextString(str));
        cell.setCellComment(comment);
    }

    private void fillFirstRow(XSSFRow firstRow, CellStyle style) {
        int columnCount = 0;
        Cell cell = firstRow.createCell(columnCount++);
        cell.setCellValue("ТИП");
        cell.setCellStyle(style);
        cell = firstRow.createCell(columnCount++);
        cell.setCellValue("Наименование");
        cell.setCellStyle(style);
        cell = firstRow.createCell(columnCount++);
        cell.setCellValue("Месяц");
        cell.setCellStyle(style);
        cell = firstRow.createCell(columnCount++);
        cell.setCellValue("Итоговый результат");
        cell.setCellStyle(style);
        for (Criteria criteria : criteriaRepo.findAllByOrderById()) {
            Cell superCell = firstRow.createCell(columnCount++);
            superCell.setCellValue(criteria.getName());
            superCell.setCellStyle(style);
        }
    }
}

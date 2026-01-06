package com.fba.logi.infrastructure.adapter.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档解析器
 * 支持 PDF 和 Excel 文件解析
 */
@Slf4j
@Component
public class DocumentParser {

    /**
     * 默认分块大小（字符数）
     */
    private static final int DEFAULT_CHUNK_SIZE = 500;

    /**
     * 默认分块重叠（字符数）
     */
    private static final int DEFAULT_CHUNK_OVERLAP = 50;

    /**
     * 解析 PDF 文件
     *
     * @param file PDF 文件
     * @return 文本内容
     */
    public String parsePdf(File file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("成功解析 PDF 文件: {}，共 {} 页", file.getName(), document.getNumberOfPages());
            return text;
        }
    }

    /**
     * 解析 PDF 输入流
     */
    public String parsePdf(InputStream inputStream) throws IOException {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 解析 Excel 文件
     *
     * @param file Excel 文件
     * @return 文本内容（每行一个记录）
     */
    public List<String> parseExcel(File file) throws IOException {
        List<String> rows = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    StringBuilder rowText = new StringBuilder();
                    for (Cell cell : row) {
                        String cellValue = getCellValueAsString(cell);
                        if (!cellValue.isEmpty()) {
                            if (rowText.length() > 0) {
                                rowText.append(" | ");
                            }
                            rowText.append(cellValue);
                        }
                    }
                    if (rowText.length() > 0) {
                        rows.add(rowText.toString());
                    }
                }
            }
            log.info("成功解析 Excel 文件: {}，共 {} 行", file.getName(), rows.size());
        }
        return rows;
    }

    /**
     * 解析 Excel 输入流
     */
    public List<String> parseExcel(InputStream inputStream) throws IOException {
        List<String> rows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    StringBuilder rowText = new StringBuilder();
                    for (Cell cell : row) {
                        String cellValue = getCellValueAsString(cell);
                        if (!cellValue.isEmpty()) {
                            if (rowText.length() > 0) {
                                rowText.append(" | ");
                            }
                            rowText.append(cellValue);
                        }
                    }
                    if (rowText.length() > 0) {
                        rows.add(rowText.toString());
                    }
                }
            }
        }
        return rows;
    }

    /**
     * 将文本分割成块
     *
     * @param text 文本内容
     * @return 文本块列表
     */
    public List<String> splitIntoChunks(String text) {
        return splitIntoChunks(text, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP);
    }

    /**
     * 将文本分割成块
     *
     * @param text      文本内容
     * @param chunkSize 块大小
     * @param overlap   重叠大小
     * @return 文本块列表
     */
    public List<String> splitIntoChunks(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        // 按段落分割
        String[] paragraphs = text.split("\\n\\n+");
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) {
                continue;
            }

            // 如果当前段落加上已有内容超过块大小
            if (currentChunk.length() + paragraph.length() > chunkSize) {
                // 保存当前块
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                }
                // 开始新块（带重叠）
                if (currentChunk.length() > overlap) {
                    String overlapText = currentChunk.substring(currentChunk.length() - overlap);
                    currentChunk = new StringBuilder(overlapText);
                } else {
                    currentChunk = new StringBuilder();
                }
            }

            // 添加段落到当前块
            if (currentChunk.length() > 0) {
                currentChunk.append("\n\n");
            }
            currentChunk.append(paragraph);
        }

        // 添加最后一个块
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        log.debug("文本分割完成，共 {} 个块", chunks.size());
        return chunks;
    }

    /**
     * 根据文件类型解析文档
     *
     * @param file 文件
     * @return 文本块列表
     */
    public List<String> parseAndSplit(File file) throws IOException {
        String fileName = file.getName().toLowerCase();
        String content;

        if (fileName.endsWith(".pdf")) {
            content = parsePdf(file);
        } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            List<String> rows = parseExcel(file);
            return rows; // Excel 每行作为一个块
        } else if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
            content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
        } else {
            throw new IllegalArgumentException("不支持的文件类型: " + fileName);
        }

        return splitIntoChunks(content);
    }

    /**
     * 获取单元格值为字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                } else {
                    double value = cell.getNumericCellValue();
                    // 整数不显示小数点
                    if (value == Math.floor(value)) {
                        yield String.valueOf((long) value);
                    }
                    yield String.valueOf(value);
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> "";
        };
    }

}

package com.astis.handbook.service;

import java.io.IOException;
import java.util.Locale;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class HandbookTextExtractionService {

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    public ExtractedHandbookText extract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Handbook file is required");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Handbook file must be 10MB or smaller");
        }

        String filename = file.getOriginalFilename() == null ? "handbook" : file.getOriginalFilename();
        String lowerFilename = filename.toLowerCase(Locale.ROOT);

        try {
            String text;
            if (lowerFilename.endsWith(".pdf")) {
                text = extractPdfText(file);
            } else if (lowerFilename.endsWith(".docx")) {
                text = extractDocxText(file);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF and DOCX handbook files are supported");
            }

            if (text == null || text.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No readable text could be extracted from this handbook");
            }

            return new ExtractedHandbookText(filename, normalizeWhitespace(text));
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Handbook text could not be extracted");
        }
    }

    private String extractPdfText(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            return new PDFTextStripper().getText(document);
        }
    }

    private String extractDocxText(MultipartFile file) throws IOException {
        try (
                XWPFDocument document = new XWPFDocument(file.getInputStream());
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)
        ) {
            return extractor.getText();
        }
    }

    private String normalizeWhitespace(String text) {
        return text.replace("\r", "\n")
                .replaceAll("[ \t]+", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }
}

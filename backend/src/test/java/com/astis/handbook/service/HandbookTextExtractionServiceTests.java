package com.astis.handbook.service;

import java.io.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HandbookTextExtractionServiceTests {

    private final HandbookTextExtractionService service = new HandbookTextExtractionService();

    @Test
    void extractReadsPdfText() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "handbook.pdf",
                "application/pdf",
                createPdf("Database coursework deadline 24 June 2028")
        );

        ExtractedHandbookText extracted = service.extract(file);

        assertThat(extracted.filename()).isEqualTo("handbook.pdf");
        assertThat(extracted.text()).contains("Database coursework deadline 24 June 2028");
    }

    @Test
    void extractReadsDocxText() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "handbook.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                createDocx("Web application coursework is worth 100%")
        );

        ExtractedHandbookText extracted = service.extract(file);

        assertThat(extracted.filename()).isEqualTo("handbook.docx");
        assertThat(extracted.text()).contains("Web application coursework is worth 100%");
    }

    @Test
    void extractRejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "handbook.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThatThrownBy(() -> service.extract(file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Handbook file is required");
    }

    @Test
    void extractRejectsUnsupportedFileExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "handbook.txt",
                "text/plain",
                "coursework".getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> service.extract(file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only PDF and DOCX");
    }

    @Test
    void extractRejectsFileLargerThanTenMegabytes() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "handbook.pdf",
                "application/pdf",
                new byte[(10 * 1024 * 1024) + 1]
        );

        assertThatThrownBy(() -> service.extract(file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("10MB or smaller");
    }

    private byte[] createPdf(String text) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(72, 720);
                content.showText(text);
                content.endText();
            }

            document.save(output);
            return output.toByteArray();
        }
    }

    private byte[] createDocx(String text) throws Exception {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText(text);
            document.write(output);
            return output.toByteArray();
        }
    }
}

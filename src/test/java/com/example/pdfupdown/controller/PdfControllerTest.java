package com.example.pdfupdown.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PdfController.class)
class PdfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final String uploadDir = "uploads";

    @BeforeEach
    void setup() {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Test
    void testUploadValidPdf() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Dummy PDF content".getBytes()
        );

        mockMvc.perform(multipart("/api/pdf/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Uploaded successfully: sample.pdf"));

        File uploadedFile = new File(uploadDir + "/sample.pdf");
        assertThat(uploadedFile.exists()).isTrue();
    }

    @Test
    void testUploadInvalidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "text.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Not a PDF".getBytes()
        );

        mockMvc.perform(multipart("/api/pdf/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only PDF files are allowed."));
    }

    @Test
    void testDownloadExistingPdf() throws Exception {
        // Prepare dummy file
        File file = new File(uploadDir + "/test.pdf");
        if (!file.exists()) {
            file.createNewFile();
        }

        mockMvc.perform(get("/api/pdf/download/test.pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=test.pdf"))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void testDownloadMissingPdf() throws Exception {
        mockMvc.perform(get("/api/pdf/download/missing.pdf"))
                .andExpect(status().isNotFound());
    }
}

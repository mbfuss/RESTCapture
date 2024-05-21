package com.example.restcapture.controllers;

import com.example.restcapture.services.CaptureService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CaptureController {
    @Autowired
    private CaptureService captureService;

    @GetMapping("/extract/{ogrn}")
    public ResponseEntity<byte[]> getExtract(@PathVariable String ogrn) throws JsonProcessingException, InterruptedException {
        // Получаем PDF от сервиса
        byte[] pdf = captureService.getExtractPdf(ogrn);

        // Устанавливаем заголовки для корректного отображения PDF в Postman
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "extract.pdf");

        // Возвращаем PDF в ответе
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}

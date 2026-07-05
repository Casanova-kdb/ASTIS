package com.astis.handbook.controller;

import com.astis.common.api.ApiResponse;
import com.astis.handbook.dto.HandbookParseResponse;
import com.astis.handbook.service.HandbookParserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/handbooks")
public class HandbookController {

    private final HandbookParserService handbookParserService;

    public HandbookController(HandbookParserService handbookParserService) {
        this.handbookParserService = handbookParserService;
    }

    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<HandbookParseResponse> parseHandbook(@RequestPart("file") MultipartFile file) {
        return ApiResponse.success("Handbook parsed", handbookParserService.parse(file));
    }
}

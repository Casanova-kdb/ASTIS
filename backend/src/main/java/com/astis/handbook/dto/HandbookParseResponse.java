package com.astis.handbook.dto;

import java.util.List;

public record HandbookParseResponse(
        String filename,
        String provider,
        boolean fallback,
        int extractedCharacterCount,
        String extractedTextPreview,
        List<HandbookDraftTaskResponse> drafts
) {
}

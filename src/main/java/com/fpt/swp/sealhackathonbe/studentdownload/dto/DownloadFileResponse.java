package com.fpt.swp.sealhackathonbe.studentdownload.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DownloadFileResponse {
    private final String filename;
    private final String contentType;
    private final byte[] content;
}

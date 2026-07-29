package com.fpt.swp.sealhackathonbe.settings.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandingPageSettingsDto {

    private List<LandingGalleryItemDto> gallery;
    private LandingFooterDto footer;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LandingGalleryItemDto {
        private String id;
        private String title;
        private String category;
        private String url;
        private String spanClass;
        private String description;
        private Boolean isFeatured;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LandingFooterDto {
        private String tagline;
        private String address;
        private String email;
        private String phone;
        private String copyright;
    }
}

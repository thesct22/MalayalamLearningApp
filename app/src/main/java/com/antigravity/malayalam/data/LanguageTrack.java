package com.antigravity.malayalam.data;

/**
 * Tracks active language path: English to Malayalam or Chinese to Malayalam.
 */
public enum LanguageTrack {
    ENGLISH_TO_MALAYALAM("en"),
    CHINESE_TO_MALAYALAM("zh");

    private final String languageCode;

    LanguageTrack(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getLanguageCode() {
        return languageCode;
    }
}

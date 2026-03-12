package com.skilltrack.common.enums;

import java.util.Arrays;
import java.util.List;

public enum MediaType {
    IMAGE("Image", Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp")),
    VIDEO("Video", Arrays.asList("mp4", "avi", "mov", "wmv", "flv", "webm", "mkv")),
    AUDIO("Audio", Arrays.asList("mp3", "wav", "ogg", "m4a", "aac", "flac")),
    DOCUMENT("Document", Arrays.asList("pdf", "doc", "docx", "txt", "rtf", "odt")),
    PRESENTATION("Presentation", Arrays.asList("ppt", "pptx", "odp")),
    SPREADSHEET("Spreadsheet", Arrays.asList("xls", "xlsx", "ods", "csv")),
    ARCHIVE("Archive", Arrays.asList("zip", "rar", "7z", "tar", "gz")),
    OTHER("Other", Arrays.asList());

    private final String displayName;
    private final List<String> supportedExtensions;

    MediaType(String displayName, List<String> supportedExtensions) {
        this.displayName = displayName;
        this.supportedExtensions = supportedExtensions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getSupportedExtensions() {
        return supportedExtensions;
    }

    public boolean supportsExtension(String extension) {
        return supportedExtensions.contains(extension.toLowerCase());
    }

    public static MediaType fromExtension(String extension) {
        String ext = extension.toLowerCase();
        if (ext.startsWith(".")) {
            ext = ext.substring(1);
        }
        
        for (MediaType type : values()) {
            if (type.supportsExtension(ext)) {
                return type;
            }
        }
        return OTHER;
    }

    public static MediaType fromContentType(String contentType) {
        if (contentType == null) {
            return OTHER;
        }
        
        String type = contentType.toLowerCase();
        
        if (type.startsWith("image/")) {
            return IMAGE;
        } else if (type.startsWith("video/")) {
            return VIDEO;
        } else if (type.startsWith("audio/")) {
            return AUDIO;
        } else if (type.contains("pdf") || type.contains("document") || type.contains("text")) {
            return DOCUMENT;
        } else if (type.contains("presentation") || type.contains("powerpoint")) {
            return PRESENTATION;
        } else if (type.contains("spreadsheet") || type.contains("excel")) {
            return SPREADSHEET;
        } else if (type.contains("zip") || type.contains("archive")) {
            return ARCHIVE;
        }
        
        return OTHER;
    }

    public boolean isViewableInBrowser() {
        return this == IMAGE || this == VIDEO || this == AUDIO || this == DOCUMENT;
    }

    public boolean requiresDownload() {
        return this == ARCHIVE || this == OTHER;
    }
}
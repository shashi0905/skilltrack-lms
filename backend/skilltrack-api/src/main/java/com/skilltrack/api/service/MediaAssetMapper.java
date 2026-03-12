package com.skilltrack.api.service;

import com.skilltrack.api.dto.response.MediaAssetResponse;
import com.skilltrack.common.entity.MediaAsset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring")
@Component
public interface MediaAssetMapper {

    @Mapping(target = "formattedFileSize", expression = "java(mediaAsset.getFormattedFileSize())")
    @Mapping(target = "uploadedByName", source = "uploadedBy.fullName")
    @Mapping(target = "downloadUrl", ignore = true)
    @Mapping(target = "previewUrl", ignore = true)
    MediaAssetResponse toResponse(MediaAsset mediaAsset);

    List<MediaAssetResponse> toResponseList(List<MediaAsset> mediaAssets);
}
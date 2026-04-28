package com.skilltrack.api.service;

import com.skilltrack.api.dto.response.LessonResponse;
import com.skilltrack.common.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MediaAssetMapper.class, QuizQuestionMapper.class})
@Component
public interface LessonMapper {

    @Mapping(target = "moduleId", source = "module.id")
    @Mapping(target = "mediaAssets", source = "mediaAssets")
    @Mapping(target = "quizQuestions", source = "quizQuestions")
    LessonResponse toResponse(Lesson lesson);

    List<LessonResponse> toResponseList(List<Lesson> lessons);
}
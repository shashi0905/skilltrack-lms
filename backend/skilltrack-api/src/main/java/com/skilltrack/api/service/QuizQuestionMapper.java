package com.skilltrack.api.service;

import com.skilltrack.api.dto.response.QuizQuestionResponse;
import com.skilltrack.common.entity.QuizQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring", uses = {QuizOptionMapper.class})
@Component
public interface QuizQuestionMapper {

    @Mapping(target = "options", source = "options")
    QuizQuestionResponse toResponse(QuizQuestion quizQuestion);

    List<QuizQuestionResponse> toResponseList(List<QuizQuestion> quizQuestions);
}
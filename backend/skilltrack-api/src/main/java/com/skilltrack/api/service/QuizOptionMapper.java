package com.skilltrack.api.service;

import com.skilltrack.api.dto.response.QuizOptionResponse;
import com.skilltrack.common.entity.QuizOption;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring")
@Component
public interface QuizOptionMapper {

    QuizOptionResponse toResponse(QuizOption quizOption);

    List<QuizOptionResponse> toResponseList(List<QuizOption> quizOptions);
}
package com.skilltrack.api.service;

import com.skilltrack.api.dto.response.ModuleResponse;
import com.skilltrack.common.entity.CourseModule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring")
@Component
public interface ModuleMapper {

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "lessonCount", expression = "java(module.getLessons().size())")
    @Mapping(target = "lessons", ignore = true)
    ModuleResponse toResponse(CourseModule module);

    List<ModuleResponse> toResponseList(List<CourseModule> modules);
}
package com.skilltrack.api.service;

import com.skilltrack.api.dto.response.CourseResponse;
import com.skilltrack.api.dto.response.ModuleResponse;
import com.skilltrack.common.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ModuleMapper.class})
@Component
public interface CourseMapper {

    @Mapping(target = "instructorName", source = "instructor.fullName")
    @Mapping(target = "instructorId", source = "instructor.id")
    @Mapping(target = "moduleCount", expression = "java(course.getModules().size())")
    @Mapping(target = "lessonCount", expression = "java(course.getTotalLessons())")
    @Mapping(target = "modules", source = "modules")
    CourseResponse toResponse(Course course);

    List<CourseResponse> toResponseList(List<Course> courses);
}
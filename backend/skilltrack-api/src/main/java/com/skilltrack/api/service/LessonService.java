package com.skilltrack.api.service;

import com.skilltrack.api.dto.request.LessonCreateRequest;
import com.skilltrack.api.dto.response.LessonResponse;
import com.skilltrack.common.entity.Course;
import com.skilltrack.common.entity.CourseModule;
import com.skilltrack.common.entity.Lesson;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.exception.BusinessException;
import com.skilltrack.common.exception.ResourceNotFoundException;
import com.skilltrack.common.repository.CourseModuleRepository;
import com.skilltrack.common.repository.CourseRepository;
import com.skilltrack.common.repository.LessonRepository;
import com.skilltrack.common.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final LessonMapper lessonMapper;

    @Autowired
    public LessonService(LessonRepository lessonRepository,
                        CourseModuleRepository moduleRepository,
                        CourseRepository courseRepository,
                        UserRepository userRepository,
                        LessonMapper lessonMapper) {
        this.lessonRepository = lessonRepository;
        this.moduleRepository = moduleRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.lessonMapper = lessonMapper;
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public LessonResponse createLesson(String courseId, String moduleId, LessonCreateRequest request, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepository.findCourseByIdAndInstructor(courseId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found or access denied"));

        CourseModule module = moduleRepository.findByIdAndCourse(moduleId, course)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        if (lessonRepository.existsByTitleAndModule(request.getTitle(), module)) {
            throw new BusinessException("A lesson with this title already exists in this module");
        }

        Lesson lesson = new Lesson(request.getTitle(), request.getDescription(), request.getContent(), module);
        lesson.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        
        Integer nextOrderIndex = lessonRepository.findNextOrderIndex(module);
        lesson.setOrderIndex(nextOrderIndex);

        module.addLesson(lesson);
        Lesson savedLesson = lessonRepository.save(lesson);
        return lessonMapper.toResponse(savedLesson);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public LessonResponse updateLesson(String courseId, String moduleId, String lessonId, LessonCreateRequest request, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepository.findCourseByIdAndInstructor(courseId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found or access denied"));

        CourseModule module = moduleRepository.findByIdAndCourse(moduleId, course)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        Lesson lesson = lessonRepository.findByIdAndModule(lessonId, module)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        if (!lesson.getTitle().equals(request.getTitle()) && 
            lessonRepository.existsByTitleAndModule(request.getTitle(), module)) {
            throw new BusinessException("A lesson with this title already exists in this module");
        }

        lesson.setTitle(request.getTitle());
        lesson.setDescription(request.getDescription());
        lesson.setContent(request.getContent());
        lesson.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());

        Lesson savedLesson = lessonRepository.save(lesson);
        return lessonMapper.toResponse(savedLesson);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public void deleteLesson(String courseId, String moduleId, String lessonId, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepository.findCourseByIdAndInstructor(courseId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found or access denied"));

        CourseModule module = moduleRepository.findByIdAndCourse(moduleId, course)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        Lesson lesson = lessonRepository.findByIdAndModule(lessonId, module)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        module.removeLesson(lesson);
        lessonRepository.delete(lesson);
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getModuleLessons(String courseId, String moduleId, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepository.findCourseByIdAndInstructor(courseId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found or access denied"));

        CourseModule module = moduleRepository.findByIdAndCourse(moduleId, course)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(module);
        return lessonMapper.toResponseList(lessons);
    }

    @Transactional(readOnly = true)
    public LessonResponse getLessonById(String courseId, String moduleId, String lessonId, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepository.findCourseByIdAndInstructor(courseId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found or access denied"));

        CourseModule module = moduleRepository.findByIdAndCourse(moduleId, course)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        Lesson lesson = lessonRepository.findByIdAndModule(lessonId, module)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        return lessonMapper.toResponse(lesson);
    }
}
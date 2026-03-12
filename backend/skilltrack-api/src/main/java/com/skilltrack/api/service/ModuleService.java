package com.skilltrack.api.service;

import com.skilltrack.api.dto.request.ModuleCreateRequest;
import com.skilltrack.api.dto.response.ModuleResponse;
import com.skilltrack.common.entity.Course;
import com.skilltrack.common.entity.CourseModule;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.exception.BusinessException;
import com.skilltrack.common.exception.ResourceNotFoundException;
import com.skilltrack.common.repository.CourseModuleRepository;
import com.skilltrack.common.repository.CourseRepository;
import com.skilltrack.common.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ModuleService {

    private final CourseModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ModuleMapper moduleMapper;

    @Autowired
    public ModuleService(CourseModuleRepository moduleRepository,
                        CourseRepository courseRepository,
                        UserRepository userRepository,
                        ModuleMapper moduleMapper) {
        this.moduleRepository = moduleRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.moduleMapper = moduleMapper;
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ModuleResponse createModule(Long courseId, ModuleCreateRequest request, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepository.findByIdAndInstructor(courseId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found or access denied"));

        if (moduleRepository.existsByTitleAndCourse(request.getTitle(), course)) {
            throw new BusinessException("A module with this title already exists in this course");
        }

        CourseModule module = new CourseModule(request.getTitle(), request.getDescription(), course);
        Integer nextOrderIndex = moduleRepository.findNextOrderIndex(course);
        module.setOrderIndex(nextOrderIndex);

        course.addModule(module);
        CourseModule savedModule = moduleRepository.save(module);
        return moduleMapper.toResponse(savedModule);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ModuleResponse updateModule(Long courseId, Long moduleId, ModuleCreateRequest request, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepository.findByIdAndInstructor(courseId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found or access denied"));

        CourseModule module = moduleRepository.findByIdAndCourse(moduleId, course)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        if (!module.getTitle().equals(request.getTitle()) && 
            moduleRepository.existsByTitleAndCourse(request.getTitle(), course)) {
            throw new BusinessException("A module with this title already exists in this course");
        }

        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());

        CourseModule savedModule = moduleRepository.save(module);
        return moduleMapper.toResponse(savedModule);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public void deleteModule(Long courseId, Long moduleId, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepository.findByIdAndInstructor(courseId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found or access denied"));

        CourseModule module = moduleRepository.findByIdAndCourse(moduleId, course)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        course.removeModule(module);
        moduleRepository.delete(module);
    }

    @Transactional(readOnly = true)
    public List<ModuleResponse> getCourseModules(Long courseId, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepository.findByIdAndInstructor(courseId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found or access denied"));

        List<CourseModule> modules = moduleRepository.findByCourseOrderByOrderIndexAsc(course);
        return moduleMapper.toResponseList(modules);
    }

    @Transactional(readOnly = true)
    public ModuleResponse getModuleById(Long courseId, Long moduleId, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepository.findByIdAndInstructor(courseId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found or access denied"));

        CourseModule module = moduleRepository.findByIdAndCourse(moduleId, course)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        return moduleMapper.toResponse(module);
    }
}
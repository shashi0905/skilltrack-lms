package com.skilltrack.api.service;

import com.skilltrack.api.dto.request.CourseCreateRequest;
import com.skilltrack.api.dto.request.CourseUpdateRequest;
import com.skilltrack.api.dto.response.CourseResponse;
import com.skilltrack.common.entity.Course;
import com.skilltrack.common.entity.CourseModule;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.CourseStatus;
import com.skilltrack.common.enums.DifficultyLevel;
import com.skilltrack.common.exception.BusinessException;
import com.skilltrack.common.exception.ResourceNotFoundException;
import com.skilltrack.common.repository.CourseRepository;
import com.skilltrack.common.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseMapper courseMapper;

    @Autowired
    public CourseService(CourseRepository courseRepository, 
                        UserRepository userRepository,
                        CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.courseMapper = courseMapper;
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public CourseResponse createCourse(CourseCreateRequest request, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        // Check for duplicate course title by same instructor
        if (courseRepository.existsByTitleAndInstructor(request.getTitle(), instructor)) {
            throw new BusinessException("A course with this title already exists");
        }

        Course course = new Course(
                request.getTitle(),
                request.getDescription(),
                request.getDifficulty(),
                instructor
        );
        course.setTags(request.getTags());
        course.setEstimatedDurationHours(request.getEstimatedDurationHours());

        Course savedCourse = courseRepository.save(course);
        return courseMapper.toResponse(savedCourse);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public CourseResponse updateCourse(String courseId, CourseUpdateRequest request, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepository.findCourseByIdAndInstructor(courseId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found or access denied"));

        // Check for duplicate title (excluding current course)
        if (!course.getTitle().equals(request.getTitle()) && 
            courseRepository.existsByTitleAndInstructor(request.getTitle(), instructor)) {
            throw new BusinessException("A course with this title already exists");
        }

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setDifficulty(request.getDifficulty());
        course.setTags(request.getTags());
        course.setEstimatedDurationHours(request.getEstimatedDurationHours());

        Course savedCourse = courseRepository.save(course);
        return courseMapper.toResponse(savedCourse);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public CourseResponse publishCourse(String courseId, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepository.findCourseByIdAndInstructor(courseId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found or access denied"));

        if (!course.canBePublished()) {
            throw new BusinessException("Course cannot be published. It must have at least one module with one lesson.");
        }

        course.publish();
        Course savedCourse = courseRepository.save(course);
        return courseMapper.toResponse(savedCourse);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public void deleteCourse(String courseId, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepository.findCourseByIdAndInstructor(courseId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found or access denied"));

        if (course.isPublished()) {
            throw new BusinessException("Published courses cannot be deleted. Please unpublish first.");
        }

        courseRepository.delete(course);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(String courseId, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepository.findCourseByIdAndInstructor(courseId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found or access denied"));

        return courseMapper.toResponse(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getInstructorCourses(String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        List<Course> courses = courseRepository.findByInstructorOrderByCreatedAtDesc(instructor);
        return courseMapper.toResponseList(courses);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getInstructorCourses(String instructorEmail, Pageable pageable) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Page<Course> courses = courseRepository.findByInstructorOrderByCreatedAtDesc(instructor, pageable);
        return courses.map(courseMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getInstructorCoursesByStatus(String instructorEmail, CourseStatus status) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        List<Course> courses = courseRepository.findByInstructorAndStatusOrderByCreatedAtDesc(instructor, status);
        return courseMapper.toResponseList(courses);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesWithDraftChanges(String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        List<Course> courses = courseRepository.findCoursesWithDraftChanges(instructor);
        return courseMapper.toResponseList(courses);
    }

    // Public methods for course catalog (no authentication required)
    @Transactional(readOnly = true)
    public Page<CourseResponse> getPublishedCourses(Pageable pageable) {
        Page<Course> courses = courseRepository.findPublishedCoursesForCatalog(pageable);
        return courses.map(courseMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> searchPublishedCourses(String searchTerm, Pageable pageable) {
        Page<Course> courses = courseRepository.searchPublishedCourses(searchTerm, pageable);
        return courses.map(courseMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getPublishedCoursesByDifficulty(DifficultyLevel difficulty, Pageable pageable) {
        Page<Course> courses = courseRepository.findByStatusAndDifficultyOrderByPublishedAtDesc(
                CourseStatus.PUBLISHED, difficulty, pageable);
        return courses.map(courseMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getPublishedCoursesByTag(String tag, Pageable pageable) {
        Page<Course> courses = courseRepository.findPublishedCoursesByTag(tag, pageable);
        return courses.map(courseMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CourseResponse getPublishedCourseById(String courseId) {
        // First, get the course with modules
        Course course = courseRepository.findPublishedCourseByIdWithModules(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Then, initialize lessons for each module within the same transaction
        // This avoids the MultipleBagFetchException while still loading all data
        for (CourseModule module : course.getModules()) {
            // Force initialization of lessons collection
            module.getLessons().size();
        }

        return courseMapper.toResponse(course);
    }

    @Transactional(readOnly = true)
    public CourseRepository.CourseStatistics getCourseStatistics(String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        return courseRepository.getCourseStatisticsByInstructor(instructor);
    }
}
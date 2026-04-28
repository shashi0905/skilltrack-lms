package com.skilltrack.api.service;

import com.skilltrack.api.dto.response.EnrollmentResponse;
import com.skilltrack.common.entity.Course;
import com.skilltrack.common.entity.Enrollment;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.CourseStatus;
import com.skilltrack.common.exception.BusinessException;
import com.skilltrack.common.exception.ResourceNotFoundException;
import com.skilltrack.common.repository.CourseRepository;
import com.skilltrack.common.repository.EnrollmentRepository;
import com.skilltrack.common.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Autowired
    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                            CourseRepository courseRepository,
                            UserRepository userRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public EnrollmentResponse enroll(String courseId, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BusinessException("Can only enroll in published courses");
        }

        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new BusinessException("Already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment(student, course);
        Enrollment saved = enrollmentRepository.save(enrollment);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return enrollmentRepository.findByStudentOrderByCreatedAtDesc(student)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public boolean isEnrolled(String courseId, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        return enrollmentRepository.existsByStudentAndCourse(student, course);
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        EnrollmentResponse r = new EnrollmentResponse();
        r.setId(enrollment.getId());
        r.setCourseId(enrollment.getCourse().getId());
        r.setCourseTitle(enrollment.getCourse().getTitle());
        r.setInstructorName(enrollment.getCourse().getInstructor().getFullName());
        r.setStatus(enrollment.getStatus());
        r.setEnrolledAt(enrollment.getCreatedAt());
        return r;
    }
}

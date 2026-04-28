package com.skilltrack.common.repository;

import com.skilltrack.common.entity.Course;
import com.skilltrack.common.entity.Enrollment;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

    boolean existsByStudentAndCourse(User student, Course course);

    Optional<Enrollment> findByStudentAndCourse(User student, Course course);

    List<Enrollment> findByStudentAndStatusOrderByCreatedAtDesc(User student, EnrollmentStatus status);

    List<Enrollment> findByStudentOrderByCreatedAtDesc(User student);

    long countByCourse(Course course);
}

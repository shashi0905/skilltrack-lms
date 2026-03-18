package com.skilltrack.common.repository;

import com.skilltrack.common.entity.Course;
import com.skilltrack.common.entity.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseModuleRepository extends JpaRepository<CourseModule, String> {

    // Find modules by course with lessons loaded
    @Query("SELECT m FROM CourseModule m LEFT JOIN FETCH m.lessons WHERE m.course = :course ORDER BY m.orderIndex ASC")
    List<CourseModule> findByCourseWithLessonsOrderByOrderIndexAsc(@Param("course") Course course);

    // Find modules by course
    List<CourseModule> findByCourseOrderByOrderIndexAsc(Course course);

    // Find module by ID and course (for ownership verification)
    Optional<CourseModule> findByIdAndCourse(String id, Course course);

    // Count modules in a course
    long countByCourse(Course course);

    // Find modules with lesson count
    @Query("SELECT m, COUNT(l) as lessonCount FROM CourseModule m " +
           "LEFT JOIN m.lessons l WHERE m.course = :course " +
           "GROUP BY m ORDER BY m.orderIndex ASC")
    List<Object[]> findModulesWithLessonCount(@Param("course") Course course);

    // Find next order index for a course
    @Query("SELECT COALESCE(MAX(m.orderIndex), 0) + 1 FROM CourseModule m WHERE m.course.id = :courseId")
    Integer findNextOrderIndex(@Param("courseId") String courseId);

    // Check if module title exists in course
    boolean existsByTitleAndCourse(String title, Course course);

    // Find modules that have lessons
    @Query("SELECT m FROM CourseModule m WHERE m.course = :course AND EXISTS (SELECT 1 FROM Lesson l WHERE l.module = m)")
    List<CourseModule> findModulesWithLessons(@Param("course") Course course);

    // Find modules without lessons
    @Query("SELECT m FROM CourseModule m WHERE m.course = :course AND NOT EXISTS (SELECT 1 FROM Lesson l WHERE l.module = m)")
    List<CourseModule> findModulesWithoutLessons(@Param("course") Course course);

    // Delete all modules by course (for cascade delete)
    void deleteByCourse(Course course);

    // Find module by order index and course
    Optional<CourseModule> findByCourseAndOrderIndex(Course course, Integer orderIndex);
}
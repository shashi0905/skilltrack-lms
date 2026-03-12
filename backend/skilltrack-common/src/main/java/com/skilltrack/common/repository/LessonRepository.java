package com.skilltrack.common.repository;

import com.skilltrack.common.entity.CourseModule;
import com.skilltrack.common.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    // Find lessons by module
    List<Lesson> findByModuleOrderByOrderIndexAsc(CourseModule module);

    // Find lesson by ID and module (for ownership verification)
    Optional<Lesson> findByIdAndModule(Long id, CourseModule module);

    // Count lessons in a module
    long countByModule(CourseModule module);

    // Find next order index for a module
    @Query("SELECT COALESCE(MAX(l.orderIndex), 0) + 1 FROM Lesson l WHERE l.module = :module")
    Integer findNextOrderIndex(@Param("module") CourseModule module);

    // Check if lesson title exists in module
    boolean existsByTitleAndModule(String title, CourseModule module);

    // Find lessons with media assets count
    @Query("SELECT l, COUNT(ma) as mediaCount FROM Lesson l " +
           "LEFT JOIN l.mediaAssets ma WHERE l.module = :module " +
           "GROUP BY l ORDER BY l.orderIndex ASC")
    List<Object[]> findLessonsWithMediaCount(@Param("module") CourseModule module);

    // Find lessons that have content
    @Query("SELECT l FROM Lesson l WHERE l.module = :module AND l.content IS NOT NULL AND l.content != ''")
    List<Lesson> findLessonsWithContent(@Param("module") CourseModule module);

    // Find lessons without content
    @Query("SELECT l FROM Lesson l WHERE l.module = :module AND (l.content IS NULL OR l.content = '')")
    List<Lesson> findLessonsWithoutContent(@Param("module") CourseModule module);

    // Delete all lessons by module (for cascade delete)
    void deleteByModule(CourseModule module);

    // Find lesson by order index and module
    Optional<Lesson> findByModuleAndOrderIndex(CourseModule module, Integer orderIndex);

    // Find all lessons for a course (across all modules)
    @Query("SELECT l FROM Lesson l JOIN l.module m WHERE m.course.id = :courseId ORDER BY m.orderIndex ASC, l.orderIndex ASC")
    List<Lesson> findAllLessonsByCourseId(@Param("courseId") Long courseId);

    // Count total lessons in a course
    @Query("SELECT COUNT(l) FROM Lesson l JOIN l.module m WHERE m.course.id = :courseId")
    long countLessonsByCourseId(@Param("courseId") Long courseId);

    // Find lessons with estimated duration
    @Query("SELECT l FROM Lesson l WHERE l.module = :module AND l.estimatedDurationMinutes IS NOT NULL ORDER BY l.orderIndex ASC")
    List<Lesson> findLessonsWithDuration(@Param("module") CourseModule module);

    // Calculate total estimated duration for a module
    @Query("SELECT COALESCE(SUM(l.estimatedDurationMinutes), 0) FROM Lesson l WHERE l.module = :module")
    Integer calculateTotalDurationForModule(@Param("module") CourseModule module);
}
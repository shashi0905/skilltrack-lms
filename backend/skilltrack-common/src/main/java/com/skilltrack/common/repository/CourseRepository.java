package com.skilltrack.common.repository;

import com.skilltrack.common.entity.Course;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.CourseStatus;
import com.skilltrack.common.enums.DifficultyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Find courses by instructor
    List<Course> findByInstructorOrderByCreatedAtDesc(User instructor);
    
    Page<Course> findByInstructorOrderByCreatedAtDesc(User instructor, Pageable pageable);

    // Find courses by status
    List<Course> findByStatusOrderByCreatedAtDesc(CourseStatus status);
    
    Page<Course> findByStatusOrderByCreatedAtDesc(CourseStatus status, Pageable pageable);

    // Find published courses for public catalog
    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND c.softDeleted = false ORDER BY c.publishedAt DESC")
    Page<Course> findPublishedCoursesForCatalog(Pageable pageable);

    // Find courses by instructor and status
    List<Course> findByInstructorAndStatusOrderByCreatedAtDesc(User instructor, CourseStatus status);

    // Find courses with draft changes
    @Query("SELECT c FROM Course c WHERE c.instructor = :instructor AND c.status = 'PUBLISHED' AND c.hasDraftChanges = true ORDER BY c.updatedAt DESC")
    List<Course> findCoursesWithDraftChanges(@Param("instructor") User instructor);

    // Search courses by title or description (for published courses)
    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND c.softDeleted = false AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY c.publishedAt DESC")
    Page<Course> searchPublishedCourses(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find courses by difficulty level
    Page<Course> findByStatusAndDifficultyOrderByPublishedAtDesc(CourseStatus status, DifficultyLevel difficulty, Pageable pageable);

    // Find courses by tags (contains search)
    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND c.softDeleted = false AND " +
           "LOWER(c.tags) LIKE LOWER(CONCAT('%', :tag, '%')) ORDER BY c.publishedAt DESC")
    Page<Course> findPublishedCoursesByTag(@Param("tag") String tag, Pageable pageable);

    // Count courses by instructor
    long countByInstructor(User instructor);

    // Count published courses by instructor
    long countByInstructorAndStatus(User instructor, CourseStatus status);

    // Check if course exists by title and instructor (for duplicate prevention)
    boolean existsByTitleAndInstructor(String title, User instructor);

    // Find course by ID and instructor (for ownership verification)
    Optional<Course> findByIdAndInstructor(Long id, User instructor);

    // Custom query to get course statistics
    @Query("SELECT COUNT(c) as totalCourses, " +
           "COUNT(CASE WHEN c.status = 'PUBLISHED' THEN 1 END) as publishedCourses, " +
           "COUNT(CASE WHEN c.status = 'DRAFT' THEN 1 END) as draftCourses " +
           "FROM Course c WHERE c.instructor = :instructor AND c.softDeleted = false")
    CourseStatistics getCourseStatisticsByInstructor(@Param("instructor") User instructor);

    // Interface for course statistics projection
    interface CourseStatistics {
        Long getTotalCourses();
        Long getPublishedCourses();
        Long getDraftCourses();
    }

    // Find courses with modules and lessons count
    @Query("SELECT c, " +
           "(SELECT COUNT(m) FROM CourseModule m WHERE m.course = c) as moduleCount, " +
           "(SELECT COUNT(l) FROM Lesson l JOIN l.module m WHERE m.course = c) as lessonCount " +
           "FROM Course c WHERE c.instructor = :instructor ORDER BY c.createdAt DESC")
    List<Object[]> findCoursesWithCounts(@Param("instructor") User instructor);

    // Find recently updated courses by instructor
    @Query("SELECT c FROM Course c WHERE c.instructor = :instructor AND c.updatedAt >= :since ORDER BY c.updatedAt DESC")
    List<Course> findRecentlyUpdatedCourses(@Param("instructor") User instructor, @Param("since") java.time.LocalDateTime since);
}
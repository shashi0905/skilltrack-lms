package com.skilltrack.common.repository;

import com.skilltrack.common.entity.Course;
import com.skilltrack.common.entity.CourseModule;
import com.skilltrack.common.entity.Lesson;
import com.skilltrack.common.entity.MediaAsset;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {

    // Find media assets by course
    List<MediaAsset> findByCourseOrderByCreatedAtDesc(Course course);

    // Find media assets by module
    List<MediaAsset> findByModuleOrderByCreatedAtDesc(CourseModule module);

    // Find media assets by lesson
    List<MediaAsset> findByLessonOrderByCreatedAtDesc(Lesson lesson);

    // Find media assets by uploader
    List<MediaAsset> findByUploadedByOrderByCreatedAtDesc(User uploadedBy);
    
    Page<MediaAsset> findByUploadedByOrderByCreatedAtDesc(User uploadedBy, Pageable pageable);

    // Find media assets by type
    List<MediaAsset> findByMediaTypeOrderByCreatedAtDesc(MediaType mediaType);

    // Find media assets by course and type
    List<MediaAsset> findByCourseAndMediaTypeOrderByCreatedAtDesc(Course course, MediaType mediaType);

    // Find media asset by storage path (for file system operations)
    Optional<MediaAsset> findByStoragePath(String storagePath);

    // Find media assets by original filename (for duplicate detection)
    List<MediaAsset> findByOriginalFilenameAndUploadedBy(String originalFilename, User uploadedBy);

    // Count media assets by course
    long countByCourse(Course course);

    // Count media assets by module
    long countByModule(CourseModule module);

    // Count media assets by lesson
    long countByLesson(Lesson lesson);

    // Count media assets by user
    long countByUploadedBy(User uploadedBy);

    // Calculate total file size by course
    @Query("SELECT COALESCE(SUM(ma.fileSizeBytes), 0) FROM MediaAsset ma WHERE ma.course = :course")
    Long calculateTotalFileSizeByCourse(@Param("course") Course course);

    // Calculate total file size by user
    @Query("SELECT COALESCE(SUM(ma.fileSizeBytes), 0) FROM MediaAsset ma WHERE ma.uploadedBy = :user")
    Long calculateTotalFileSizeByUser(@Param("user") User user);

    // Find large files (above certain size)
    @Query("SELECT ma FROM MediaAsset ma WHERE ma.fileSizeBytes > :sizeThreshold ORDER BY ma.fileSizeBytes DESC")
    List<MediaAsset> findLargeFiles(@Param("sizeThreshold") Long sizeThreshold);

    // Find recent uploads
    @Query("SELECT ma FROM MediaAsset ma WHERE ma.createdAt >= :since ORDER BY ma.createdAt DESC")
    List<MediaAsset> findRecentUploads(@Param("since") LocalDateTime since);

    // Find orphaned media assets (not attached to any course, module, or lesson)
    @Query("SELECT ma FROM MediaAsset ma WHERE ma.course IS NULL AND ma.module IS NULL AND ma.lesson IS NULL")
    List<MediaAsset> findOrphanedMediaAssets();

    // Find media assets by content type pattern
    @Query("SELECT ma FROM MediaAsset ma WHERE ma.contentType LIKE :contentTypePattern ORDER BY ma.createdAt DESC")
    List<MediaAsset> findByContentTypePattern(@Param("contentTypePattern") String contentTypePattern);

    // Delete media assets by course (for cascade delete)
    void deleteByCourse(Course course);

    // Delete media assets by module (for cascade delete)
    void deleteByModule(CourseModule module);

    // Delete media assets by lesson (for cascade delete)
    void deleteByLesson(Lesson lesson);

    // Find media assets uploaded by instructor for a specific course
    @Query("SELECT ma FROM MediaAsset ma WHERE ma.course = :course AND ma.uploadedBy = :instructor ORDER BY ma.createdAt DESC")
    List<MediaAsset> findByCourseAndInstructor(@Param("course") Course course, @Param("instructor") User instructor);

    // Get media type statistics for a course
    @Query("SELECT ma.mediaType, COUNT(ma), COALESCE(SUM(ma.fileSizeBytes), 0) " +
           "FROM MediaAsset ma WHERE ma.course = :course GROUP BY ma.mediaType")
    List<Object[]> getMediaTypeStatisticsByCourse(@Param("course") Course course);

    // Find media assets that need cleanup (older than specified date and orphaned)
    @Query("SELECT ma FROM MediaAsset ma WHERE ma.createdAt < :cutoffDate AND " +
           "ma.course IS NULL AND ma.module IS NULL AND ma.lesson IS NULL")
    List<MediaAsset> findMediaAssetsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);
}
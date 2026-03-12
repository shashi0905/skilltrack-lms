package com.skilltrack.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course_modules")
public class CourseModule extends BaseEntity {

    @NotBlank(message = "Module title is required")
    @Size(max = 200, message = "Module title must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Module description is required")
    @Size(max = 1000, message = "Module description must not exceed 1000 characters")
    @Column(nullable = false, length = 1000)
    private String description;

    @NotNull(message = "Order index is required")
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @NotNull(message = "Course is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<Lesson> lessons = new ArrayList<>();

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MediaAsset> mediaAssets = new ArrayList<>();

    // Constructors
    public CourseModule() {}

    public CourseModule(String title, String description, Course course) {
        this.title = title;
        this.description = description;
        this.course = course;
    }

    // Business methods
    public boolean hasLessons() {
        return !lessons.isEmpty();
    }

    public int getLessonCount() {
        return lessons.size();
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        if (course != null) {
            course.markAsDraftChanged();
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        if (course != null) {
            course.markAsDraftChanged();
        }
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    public List<MediaAsset> getMediaAssets() {
        return mediaAssets;
    }

    public void setMediaAssets(List<MediaAsset> mediaAssets) {
        this.mediaAssets = mediaAssets;
    }

    // Helper methods for managing lessons
    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
        lesson.setModule(this);
        lesson.setOrderIndex(lessons.size());
        if (course != null) {
            course.markAsDraftChanged();
        }
    }

    public void removeLesson(Lesson lesson) {
        lessons.remove(lesson);
        lesson.setModule(null);
        // Reorder remaining lessons
        for (int i = 0; i < lessons.size(); i++) {
            lessons.get(i).setOrderIndex(i + 1);
        }
        if (course != null) {
            course.markAsDraftChanged();
        }
    }

    // Helper methods for managing media assets
    public void addMediaAsset(MediaAsset mediaAsset) {
        mediaAssets.add(mediaAsset);
        mediaAsset.setModule(this);
        if (course != null) {
            course.markAsDraftChanged();
        }
    }

    public void removeMediaAsset(MediaAsset mediaAsset) {
        mediaAssets.remove(mediaAsset);
        mediaAsset.setModule(null);
        if (course != null) {
            course.markAsDraftChanged();
        }
    }
}
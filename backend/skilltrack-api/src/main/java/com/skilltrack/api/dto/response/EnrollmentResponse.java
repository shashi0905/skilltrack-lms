package com.skilltrack.api.dto.response;

import com.skilltrack.common.enums.EnrollmentStatus;
import java.time.LocalDateTime;

public class EnrollmentResponse {
    private String id;
    private String courseId;
    private String courseTitle;
    private String instructorName;
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;

    public EnrollmentResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }
}

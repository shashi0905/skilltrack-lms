package com.skilltrack.common.repository;

import com.skilltrack.common.entity.Lesson;
import com.skilltrack.common.entity.QuizQuestion;
import com.skilltrack.common.enums.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    // Find questions by lesson
    List<QuizQuestion> findByLessonOrderByOrderIndexAsc(Lesson lesson);

    // Find question by ID and lesson
    Optional<QuizQuestion> findByIdAndLesson(String id, Lesson lesson);

    // Find questions by type
    List<QuizQuestion> findByQuestionTypeOrderByOrderIndexAsc(QuestionType questionType);

    // Count questions by lesson
    long countByLesson(Lesson lesson);

    // Find next order index for a lesson
    @Query("SELECT COALESCE(MAX(q.orderIndex), 0) + 1 FROM QuizQuestion q WHERE q.lesson = :lesson")
    Integer findNextOrderIndex(@Param("lesson") Lesson lesson);

    // Check if question text exists in lesson
    boolean existsByQuestionTextAndLesson(String questionText, Lesson lesson);

    // Delete questions by lesson (for cascade delete)
    void deleteByLesson(Lesson lesson);

    // Get total points for a lesson
    @Query("SELECT COALESCE(SUM(q.points), 0) FROM QuizQuestion q WHERE q.lesson = :lesson")
    Integer getTotalPointsByLesson(@Param("lesson") Lesson lesson);

    // Find questions with no correct answers
    @Query("SELECT q FROM QuizQuestion q WHERE q.lesson = :lesson AND " +
           "NOT EXISTS (SELECT o FROM QuizOption o WHERE o.question = q AND o.isCorrect = true)")
    List<QuizQuestion> findQuestionsWithoutCorrectAnswers(@Param("lesson") Lesson lesson);

    // Get question statistics by lesson
    @Query("SELECT q.questionType, COUNT(q) FROM QuizQuestion q WHERE q.lesson = :lesson GROUP BY q.questionType")
    List<Object[]> getQuestionTypeStatisticsByLesson(@Param("lesson") Lesson lesson);
}
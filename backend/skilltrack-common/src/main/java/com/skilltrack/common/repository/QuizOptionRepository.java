package com.skilltrack.common.repository;

import com.skilltrack.common.entity.QuizOption;
import com.skilltrack.common.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {

    // Find options by question
    List<QuizOption> findByQuestionOrderByOrderIndexAsc(QuizQuestion question);

    // Find option by ID and question
    Optional<QuizOption> findByIdAndQuestion(String id, QuizQuestion question);

    // Find correct options for a question
    List<QuizOption> findByQuestionAndIsCorrectTrueOrderByOrderIndexAsc(QuizQuestion question);

    // Count options by question
    long countByQuestion(QuizQuestion question);

    // Count correct options by question
    long countByQuestionAndIsCorrectTrue(QuizQuestion question);

    // Find next order index for a question
    @Query("SELECT COALESCE(MAX(o.orderIndex), 0) + 1 FROM QuizOption o WHERE o.question = :question")
    Integer findNextOrderIndex(@Param("question") QuizQuestion question);

    // Check if option text exists in question
    boolean existsByOptionTextAndQuestion(String optionText, QuizQuestion question);

    // Delete options by question (for cascade delete)
    void deleteByQuestion(QuizQuestion question);

    // Validate that single choice questions have exactly one correct answer
    @Query("SELECT COUNT(o) FROM QuizOption o WHERE o.question = :question AND o.isCorrect = true")
    long countCorrectOptionsByQuestion(@Param("question") QuizQuestion question);
}
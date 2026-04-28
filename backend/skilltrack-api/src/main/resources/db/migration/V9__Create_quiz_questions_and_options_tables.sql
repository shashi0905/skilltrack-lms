-- V9__Create_quiz_questions_and_options_tables.sql

-- Create quiz_questions table
CREATE TABLE quiz_questions (
    id VARCHAR(36) PRIMARY KEY,
    lesson_id VARCHAR(36) NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    question_text VARCHAR(1000) NOT NULL,
    question_type VARCHAR(20) NOT NULL CHECK (question_type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE')),
    order_index INTEGER NOT NULL,
    explanation VARCHAR(500),
    points INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    soft_deleted BOOLEAN DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create quiz_options table
CREATE TABLE quiz_options (
    id VARCHAR(36) PRIMARY KEY,
    question_id VARCHAR(36) NOT NULL REFERENCES quiz_questions(id) ON DELETE CASCADE,
    option_text VARCHAR(500) NOT NULL,
    order_index INTEGER NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    soft_deleted BOOLEAN DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create indexes
CREATE INDEX idx_quiz_questions_lesson_id ON quiz_questions(lesson_id);
CREATE INDEX idx_quiz_questions_order_index ON quiz_questions(order_index);
CREATE INDEX idx_quiz_questions_question_type ON quiz_questions(question_type);
CREATE UNIQUE INDEX idx_quiz_questions_unique_order ON quiz_questions(lesson_id, order_index) WHERE soft_deleted = FALSE;

CREATE INDEX idx_quiz_options_question_id ON quiz_options(question_id);
CREATE INDEX idx_quiz_options_order_index ON quiz_options(order_index);
CREATE INDEX idx_quiz_options_is_correct ON quiz_options(is_correct);
CREATE UNIQUE INDEX idx_quiz_options_unique_order ON quiz_options(question_id, order_index) WHERE soft_deleted = FALSE;

-- Create triggers for updated_at
CREATE TRIGGER update_quiz_questions_updated_at BEFORE UPDATE ON quiz_questions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_quiz_options_updated_at BEFORE UPDATE ON quiz_options FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
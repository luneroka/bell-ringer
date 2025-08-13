-- db/seed_from_json.sql (updated)
-- Idempotent JSON -> relational import, including SHORT_ANSWER into open_answers.
-- Expects: psql -v seed='...'

BEGIN;

-- Single statement using data-modifying CTEs so we can reuse computed sets
WITH seed AS (
  SELECT (:'seed')::jsonb AS j
),
ver AS (
  SELECT j->>'version' AS version FROM seed
),

-- Upsert categories by slug
cats AS (
  INSERT INTO categories (name, slug, description)
  SELECT c.name, c.slug, c.description
  FROM seed s,
       jsonb_to_recordset(s.j->'categories')
       AS c(name text, slug text, description text, questions jsonb)
  ON CONFLICT (slug)
  DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description
  RETURNING id, slug
),

-- Prepare questions joined to their category IDs (also expose shortAnswer JSON)
q_src AS (
  SELECT
    ca.id AS category_id,
    q.type::text         AS type_txt,
    q.difficulty::text   AS difficulty_txt,
    q.question::text     AS question_txt,
    COALESCE(q.keywords, '[]'::jsonb) AS keywords_jb,
    q.choices            AS choices_jb,
    q.explanations::text AS explanations_txt,
    q.shortAnswer        AS short_jb
  FROM seed s
  JOIN LATERAL jsonb_to_recordset(s.j->'categories')
       AS cj(name text, slug text, description text, questions jsonb)
       ON TRUE
  JOIN cats ca ON ca.slug = cj.slug
  JOIN LATERAL jsonb_to_recordset(cj.questions)
       AS q(
         type text,
         difficulty text,
         question text,
         keywords jsonb,
         choices jsonb,
         explanations text,
         shortAnswer jsonb
       ) ON TRUE
),

-- Upsert questions by (category_id, question)
upq AS (
  INSERT INTO questions (category_id, type, difficulty, question, keywords, explanations)
  SELECT
    category_id,
    type_txt::question_type,
    difficulty_txt::difficulty,
    question_txt,
    keywords_jb,
    explanations_txt
  FROM q_src
  ON CONFLICT (category_id, question)
  DO UPDATE SET
    type = EXCLUDED.type,
    difficulty = EXCLUDED.difficulty,
    keywords = EXCLUDED.keywords,
    explanations = EXCLUDED.explanations
  RETURNING id AS question_id, category_id, question, type
),

-- Clear existing choices for these questions (safe no-op for SHORT_ANSWER)
del_old_choices AS (
  DELETE FROM choices ch
  USING upq
  WHERE ch.question_id = upq.question_id
  RETURNING 1
),

-- Re-insert choices for NON-short-answer questions
ins_choices AS (
  INSERT INTO choices (question_id, choice_text, is_correct)
  SELECT
    u.question_id,
    ch->>'text',
    (ch->>'isCorrect')::boolean
  FROM seed s
  JOIN LATERAL jsonb_to_recordset(s.j->'categories')
       AS cj(name text, slug text, description text, questions jsonb)
       ON TRUE
  JOIN cats ca ON ca.slug = cj.slug
  JOIN LATERAL jsonb_to_recordset(cj.questions)
       AS q(type text, difficulty text, question text, keywords jsonb, choices jsonb, explanations text, shortAnswer jsonb)
       ON TRUE
  JOIN upq u ON u.category_id = ca.id AND u.question = q.question
  JOIN LATERAL jsonb_array_elements(q.choices) AS ch ON TRUE
  WHERE u.type <> 'SHORT_ANSWER'::question_type
  RETURNING 1
),

-- Clear any existing open_answers for SHORT_ANSWER questions
del_old_open_answers AS (
  DELETE FROM open_answers oa
  USING upq
  WHERE oa.question_id = upq.question_id
    AND upq.type = 'SHORT_ANSWER'::question_type
  RETURNING 1
),

-- Insert fresh open_answers for SHORT_ANSWER questions
ins_open_answers AS (
  INSERT INTO open_answers (question_id, answer, rubric_keywords, min_score)
  SELECT
    u.question_id,
    q.shortAnswer->>'answer' AS answer,
    (q.shortAnswer->'rubric_keywords')::jsonb AS rubric_keywords,
    NULLIF(q.shortAnswer->>'min_score','')::int AS min_score
  FROM seed s
  JOIN LATERAL jsonb_to_recordset(s.j->'categories')
       AS cj(name text, slug text, description text, questions jsonb)
       ON TRUE
  JOIN cats ca ON ca.slug = cj.slug
  JOIN LATERAL jsonb_to_recordset(cj.questions)
       AS q(type text, difficulty text, question text, keywords jsonb, choices jsonb, explanations text, shortAnswer jsonb)
       ON TRUE
  JOIN upq u ON u.category_id = ca.id AND u.question = q.question
  WHERE u.type = 'SHORT_ANSWER'::question_type
  RETURNING 1
)
SELECT 1;

-- Mark the seed version as applied (idempotent)
INSERT INTO seed_migrations (seed_version)
SELECT version FROM ver
ON CONFLICT (seed_version) DO NOTHING;

COMMIT;
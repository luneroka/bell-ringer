-- Import script that reads JSON from file
-- Usage: psql -U user -d database -f import_seed.sql

BEGIN;

-- Read JSON content from file and store in temporary table
CREATE TEMP TABLE temp_seed_json AS
SELECT pg_read_file('/tmp/seed.json')::jsonb AS j;

-- Single statement using data-modifying CTEs so we can reuse computed sets
WITH seed AS (
  SELECT j FROM temp_seed_json
),
ver AS (
  SELECT j->>'version' AS version FROM seed
),

-- First, create parent categories
parent_cats AS (
  INSERT INTO categories (name, slug, created_at, updated_at)
  SELECT DISTINCT 
    CASE 
      WHEN c.slug LIKE 'frontend-%' THEN 'Frontend'
      WHEN c.slug LIKE 'backend-%' THEN 'Backend'
      WHEN c.slug LIKE 'data-%' THEN 'Data'
      WHEN c.slug LIKE 'devops-%' THEN 'DevOps'
    END as name,
    CASE 
      WHEN c.slug LIKE 'frontend-%' THEN 'frontend'
      WHEN c.slug LIKE 'backend-%' THEN 'backend'
      WHEN c.slug LIKE 'data-%' THEN 'data'
      WHEN c.slug LIKE 'devops-%' THEN 'devops'
    END as slug,
    NOW(),
    NOW()
  FROM seed s,
       jsonb_to_recordset(s.j->'categories')
       AS c(name text, slug text, description text, questions jsonb)
  WHERE c.slug ~ '^(frontend|backend|data|devops)-'
  ON CONFLICT (slug) DO NOTHING
  RETURNING id, slug
),

-- Then create child categories with proper parent references
cats AS (
  INSERT INTO categories (name, slug, parent_id, created_at, updated_at)
  SELECT 
    c.name, 
    c.slug,
    p.id,
    NOW(),
    NOW()
  FROM seed s,
       jsonb_to_recordset(s.j->'categories')
       AS c(name text, slug text, description text, questions jsonb)
  JOIN parent_cats p ON (
    (c.slug LIKE 'frontend-%' AND p.slug = 'frontend') OR
    (c.slug LIKE 'backend-%' AND p.slug = 'backend') OR
    (c.slug LIKE 'data-%' AND p.slug = 'data') OR
    (c.slug LIKE 'devops-%' AND p.slug = 'devops')
  )
  ON CONFLICT (slug)
  DO UPDATE SET
    name = EXCLUDED.name
  RETURNING id, slug
),

-- Prepare questions joined to their category IDs (also expose shortAnswer JSON)
q_src AS (
  SELECT
    ca.id AS category_id,
    q.type::text         AS type_txt,
    q.difficulty::text   AS difficulty_txt,
    q.question::text     AS question_txt,
    q.choices            AS choices_jb,
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

-- Insert questions (allowing duplicates for now)
upq AS (
  INSERT INTO questions (category_id, type, difficulty, question, created_at, updated_at)
  SELECT DISTINCT
    category_id,
    type_txt,
    difficulty_txt,
    question_txt,
    NOW(),
    NOW()
  FROM q_src
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
  WHERE u.type <> 'SHORT_ANSWER'
  ON CONFLICT (question_id, choice_text) DO NOTHING
  RETURNING 1
),

-- Clear any existing open_answers for SHORT_ANSWER questions
del_old_open_answers AS (
  DELETE FROM open_answers oa
  USING upq
  WHERE oa.question_id = upq.question_id
    AND upq.type = 'SHORT_ANSWER'
  RETURNING 1
),

-- Insert fresh open_answers for SHORT_ANSWER questions
ins_open_answers AS (
  INSERT INTO open_answers (question_id, answer, rubric_keywords, min_score)
  SELECT DISTINCT
    u.question_id,
    jq.short_answer_data->>'answer' AS answer,
    jq.short_answer_data->'rubric_keywords' AS rubric_keywords,
    COALESCE((jq.short_answer_data->>'min_score')::int, 70) AS min_score
  FROM seed s,
       jsonb_to_recordset(s.j->'categories') AS cj(name text, slug text, questions jsonb),
       jsonb_array_elements(cj.questions) AS q_elem,
       LATERAL (SELECT 
         cj.slug as category_slug,
         q_elem->>'type' as q_type,
         q_elem->>'question' as q_text,
         q_elem->'shortAnswer' as short_answer_data
       ) AS jq
  JOIN cats ca ON ca.slug = jq.category_slug
  JOIN upq u ON u.category_id = ca.id AND u.question = jq.q_text
  WHERE jq.q_type = 'SHORT_ANSWER' 
    AND jq.short_answer_data IS NOT NULL
    AND jq.short_answer_data->>'answer' IS NOT NULL
    AND trim(jq.short_answer_data->>'answer') <> ''
  RETURNING 1
)
SELECT 
  (SELECT COUNT(*) FROM parent_cats) + (SELECT COUNT(*) FROM cats) AS categories_imported,
  (SELECT COUNT(*) FROM upq) AS questions_imported,
  (SELECT COUNT(*) FROM ins_choices) AS choices_imported,
  (SELECT COUNT(*) FROM ins_open_answers) AS open_answers_imported;

-- Mark the seed version as applied (idempotent)
-- First, ensure the seed_migrations table exists
CREATE TABLE IF NOT EXISTS seed_migrations (
  seed_version VARCHAR(255) PRIMARY KEY,
  applied_at TIMESTAMP DEFAULT NOW()
);

INSERT INTO seed_migrations (seed_version)
SELECT (SELECT j->>'version' FROM temp_seed_json)
ON CONFLICT (seed_version) DO NOTHING;

COMMIT;

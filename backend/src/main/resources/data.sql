-- =======================================================
-- Bell-Ringer: Seed 24 React Questions for Testing (Postgres)
-- =======================================================
-- Use as Spring Boot startup data:
--   Save as: src/main/resources/data.sql
-- Or run manually via psql:
--   psql -d your_database -f seed_questions_large.sql
-- NOTE: This file uses category_id = 2.
--       Replace all occurrences of 2 with your actual childCategoryId if needed.
--       (Search/replace: ' category_id, ' lines and ' WHERE category_id = 2 ' checks.)
-- Safe to re-run in dev if you clear previous seed rows for category_id = 2.
-- -----------------------------------------------------------------------
-- OPTIONAL: Clean (uncomment carefully in a dev DB)
-- DELETE FROM attempt_answers;
-- DELETE FROM attempts;
-- DELETE FROM quiz_questions;
-- DELETE FROM choices WHERE question_id IN (SELECT id FROM questions WHERE category_id = 2);
-- DELETE FROM answers WHERE question_id IN (SELECT id FROM questions WHERE category_id = 2);
-- DELETE FROM questions WHERE category_id = 2;
-- -----------------------------------------------------------------------

-- Ensure parent and child categories exist (idempotent)
INSERT INTO categories(name, slug, area, parent_id, created_at, updated_at)
VALUES ('Frontend', 'frontend', 'Frontend', NULL, NOW(), NOW())
ON CONFLICT (slug)
    DO UPDATE SET name = EXCLUDED.name, area = EXCLUDED.area, updated_at = NOW();

INSERT INTO categories(name, slug, area, parent_id, created_at, updated_at)
VALUES ('React', 'react', 'Frontend', (SELECT id FROM categories WHERE slug = 'frontend'), NOW(), NOW())
ON CONFLICT (slug)
    DO UPDATE SET name = EXCLUDED.name,
                  area = EXCLUDED.area,
                  parent_id = (SELECT id FROM categories WHERE slug = 'frontend'),
                  updated_at = NOW();

-- =======================
-- EASY (8 questions)
-- =======================

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'react'), 'EASY', 'What is React primarily used for?', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('Building user interfaces', TRUE),
  ('Managing databases', FALSE),
  ('Server-side rendering only', FALSE),
  ('Styling with CSS only', FALSE)
) AS v(txt, ok);

-- E2 TRUE_FALSE (fixed)
WITH q AS (
    INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
        VALUES (
                   'TRUE_FALSE',
                   (SELECT id FROM categories WHERE slug = 'react'),
                   'EASY',
                   'React uses a virtual DOM to optimize updates.',
                   NOW(), NOW()
               )
        RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('True', TRUE), ('False', FALSE)) AS v(txt, ok);

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'react'), 'EASY', 'Which of the following are React features?', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('Component-based architecture', TRUE),
  ('One-way data flow', TRUE),
  ('Built-in routing', FALSE),
  ('Template-based rendering like AngularJS', FALSE)
) AS v(txt, ok);

INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'react'), 'EASY', 'Which hook is used to manage state in a function component?', NOW(), NOW());
/*INSERT INTO answers(question_id, answer) SELECT id, 'useState' FROM q;*/

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'react'), 'EASY', 'JSX compiles down to calls to which function by default?', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('React.createElement', TRUE),
  ('document.createElement', FALSE),
  ('createJSX', FALSE),
  ('renderJSX', FALSE)
) AS v(txt, ok);

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('TRUE_FALSE', (SELECT id FROM categories WHERE slug = 'react'), 'EASY', 'Props are mutable from inside the receiving component.', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES ('True', FALSE), ('False', TRUE)) AS v(txt, ok);

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'react'), 'EASY', 'Pick valid places to use JSX expressions.', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('Inside component return()', TRUE),
  ('Inside attribute values', TRUE),
  ('Outside any React file only', FALSE),
  ('In plain HTML files at runtime', FALSE)
) AS v(txt, ok);

INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'react'), 'EASY', 'Name the hook used to access a mutable value that persists across renders without causing re-renders.', NOW(), NOW());
/*INSERT INTO answers(question_id, answer) SELECT id, 'useRef' FROM q;*/

-- =======================
-- MEDIUM (8 questions)
-- =======================

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'react'), 'MEDIUM', 'What prop must be supplied when rendering lists in React?', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('key', TRUE),
  ('id', FALSE),
  ('index', FALSE),
  ('ref', FALSE)
) AS v(txt, ok);

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('TRUE_FALSE', (SELECT id FROM categories WHERE slug = 'react'), 'MEDIUM', 'useEffect runs synchronously before the browser paints.', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES ('True', FALSE), ('False', TRUE)) AS v(txt, ok);

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'react'), 'MEDIUM', 'Which hooks trigger side effects or lifecycle-like behavior?', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('useEffect', TRUE),
  ('useLayoutEffect', TRUE),
  ('useMemo', FALSE),
  ('useId', FALSE)
) AS v(txt, ok);

INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'react'), 'MEDIUM', 'What hook memoizes a computed value based on dependencies?', NOW(), NOW());
/*INSERT INTO answers(question_id, answer) SELECT id, 'useMemo' FROM q;*/

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'react'), 'MEDIUM', 'Which pattern helps avoid unnecessary re-renders when passing callbacks?', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('useCallback', TRUE),
  ('Passing new inline functions every render', FALSE),
  ('Mutating props before passing', FALSE),
  ('Using global variables', FALSE)
) AS v(txt, ok);

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('TRUE_FALSE', (SELECT id FROM categories WHERE slug = 'react'), 'MEDIUM', 'Context provides a way to pass data through the component tree without prop drilling.', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES ('True', TRUE), ('False', FALSE)) AS v(txt, ok);

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'react'), 'MEDIUM', 'Which are valid ways to optimize expensive recalculations?', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('useMemo', TRUE),
  ('Memoizing selectors', TRUE),
  ('Mutating React state directly', FALSE),
  ('Re-rendering the whole tree', FALSE)
) AS v(txt, ok);

INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'react'), 'MEDIUM', 'Which hook is used to imperatively access a child component DOM node?', NOW(), NOW());
/*INSERT INTO answers(question_id, answer) SELECT id, 'useRef' FROM q;*/

-- =======================
-- HARD (8 questions)
-- =======================

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'react'), 'HARD', 'What reconciler strategy helps React efficiently update the UI?', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('Diffing with heuristics', TRUE),
  ('Full DOM re-render on every change', FALSE),
  ('Manual DOM patching by the developer', FALSE),
  ('Shadow DOM from the browser', FALSE)
) AS v(txt, ok);

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('TRUE_FALSE', (SELECT id FROM categories WHERE slug = 'react'), 'HARD', 'useLayoutEffect runs after the browser paints the screen.', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES ('True', FALSE), ('False', TRUE)) AS v(txt, ok);

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'react'), 'HARD', 'Which patterns help avoid unnecessary re-renders?', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('React.memo', TRUE),
  ('useCallback', TRUE),
  ('Always inline arrow functions in JSX', FALSE),
  ('Mutating props before passing', FALSE)
) AS v(txt, ok);

INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'react'), 'HARD', 'Name the hook that memoizes a callback function.', NOW(), NOW());
/*INSERT INTO answers(question_id, answer) SELECT id, 'useCallback' FROM q;*/

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'react'), 'HARD', 'Which scheduling mechanism can React use to prioritize rendering?', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('Cooperative scheduling', TRUE),
  ('Always synchronous blocking', FALSE),
  ('Global microtask queue only', FALSE),
  ('Mutation observers only', FALSE)
) AS v(txt, ok);

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('TRUE_FALSE', (SELECT id FROM categories WHERE slug = 'react'), 'HARD', 'Keys help React identify which items have changed in a list.', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES ('True', TRUE), ('False', FALSE)) AS v(txt, ok);

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'react'), 'HARD', 'Pick techniques to prevent prop-drilling or manage state at scale.', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('Context', TRUE),
  ('Redux or other state libs', TRUE),
  ('Mutating state in-place', FALSE),
  ('Keeping all state in the root only', FALSE)
) AS v(txt, ok);

INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'react'), 'HARD', 'Which hook memoizes a function and returns a stable reference across renders?', NOW(), NOW());
/*INSERT INTO answers(question_id, answer) SELECT id, 'useCallback' FROM q;*/

-- =======================
-- Sanity checks (optional)
-- =======================
-- SELECT difficulty, type, COUNT(*) FROM questions WHERE category_id = (SELECT id FROM categories WHERE slug = 'react') GROUP BY difficulty, type ORDER BY difficulty, type;
-- SELECT COUNT(*) FROM choices WHERE question_id IN (SELECT id FROM questions WHERE category_id = (SELECT id FROM categories WHERE slug = 'react'));
-- SELECT * FROM questions WHERE category_id = (SELECT id FROM categories WHERE slug = 'react') ORDER BY id;

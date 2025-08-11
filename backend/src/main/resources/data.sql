
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

-- =======================
-- EASY (8 questions)
-- =======================

-- E1 UNIQUE_CHOICE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('UNIQUE_CHOICE', 2, 'EASY', 'What is React primarily used for?')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('Building user interfaces', TRUE),
  ('Managing databases', FALSE),
  ('Server-side rendering only', FALSE),
  ('Styling with CSS only', FALSE)
) AS v(txt, ok);

-- E2 TRUE_FALSE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('TRUE_FALSE', 2, 'EASY', 'React uses a virtual DOM to optimize updates.')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES ('True', TRUE), ('False', FALSE)) AS v(txt, ok);

-- E3 MULTIPLE_CHOICE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('MULTIPLE_CHOICE', 2, 'EASY', 'Which of the following are React features?')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('Component-based architecture', TRUE),
  ('One-way data flow', TRUE),
  ('Built-in routing', FALSE),
  ('Template-based rendering like AngularJS', FALSE)
) AS v(txt, ok);

-- E4 SHORT_ANSWER
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('SHORT_ANSWER', 2, 'EASY', 'Which hook is used to manage state in a function component?')
  RETURNING id
)
INSERT INTO answers(question_id, answer) SELECT id, 'useState' FROM q;

-- E5 UNIQUE_CHOICE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('UNIQUE_CHOICE', 2, 'EASY', 'JSX compiles down to calls to which function by default?')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('React.createElement', TRUE),
  ('document.createElement', FALSE),
  ('createJSX', FALSE),
  ('renderJSX', FALSE)
) AS v(txt, ok);

-- E6 TRUE_FALSE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('TRUE_FALSE', 2, 'EASY', 'Props are mutable from inside the receiving component.')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES ('True', FALSE), ('False', TRUE)) AS v(txt, ok);

-- E7 MULTIPLE_CHOICE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('MULTIPLE_CHOICE', 2, 'EASY', 'Pick valid places to use JSX expressions.')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('Inside component return()', TRUE),
  ('Inside attribute values', TRUE),
  ('Outside any React file only', FALSE),
  ('In plain HTML files at runtime', FALSE)
) AS v(txt, ok);

-- E8 SHORT_ANSWER
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('SHORT_ANSWER', 2, 'EASY', 'Name the hook used to access a mutable value that persists across renders without causing re-renders.')
  RETURNING id
)
INSERT INTO answers(question_id, answer) SELECT id, 'useRef' FROM q;

-- =======================
-- MEDIUM (8 questions)
-- =======================

-- M1 UNIQUE_CHOICE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('UNIQUE_CHOICE', 2, 'MEDIUM', 'What prop must be supplied when rendering lists in React?')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('key', TRUE),
  ('id', FALSE),
  ('index', FALSE),
  ('ref', FALSE)
) AS v(txt, ok);

-- M2 TRUE_FALSE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('TRUE_FALSE', 2, 'MEDIUM', 'useEffect runs synchronously before the browser paints.')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES ('True', FALSE), ('False', TRUE)) AS v(txt, ok);

-- M3 MULTIPLE_CHOICE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('MULTIPLE_CHOICE', 2, 'MEDIUM', 'Which hooks trigger side effects or lifecycle-like behavior?')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('useEffect', TRUE),
  ('useLayoutEffect', TRUE),
  ('useMemo', FALSE),
  ('useId', FALSE)
) AS v(txt, ok);

-- M4 SHORT_ANSWER
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('SHORT_ANSWER', 2, 'MEDIUM', 'What hook memoizes a computed value based on dependencies?')
  RETURNING id
)
INSERT INTO answers(question_id, answer) SELECT id, 'useMemo' FROM q;

-- M5 UNIQUE_CHOICE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('UNIQUE_CHOICE', 2, 'MEDIUM', 'Which pattern helps avoid unnecessary re-renders when passing callbacks?')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('useCallback', TRUE),
  ('Passing new inline functions every render', FALSE),
  ('Mutating props before passing', FALSE),
  ('Using global variables', FALSE)
) AS v(txt, ok);

-- M6 TRUE_FALSE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('TRUE_FALSE', 2, 'MEDIUM', 'Context provides a way to pass data through the component tree without prop drilling.')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES ('True', TRUE), ('False', FALSE)) AS v(txt, ok);

-- M7 MULTIPLE_CHOICE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('MULTIPLE_CHOICE', 2, 'MEDIUM', 'Which are valid ways to optimize expensive recalculations?')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('useMemo', TRUE),
  ('Memoizing selectors', TRUE),
  ('Mutating React state directly', FALSE),
  ('Re-rendering the whole tree', FALSE)
) AS v(txt, ok);

-- M8 SHORT_ANSWER
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('SHORT_ANSWER', 2, 'MEDIUM', 'Which hook is used to imperatively access a child component DOM node?')
  RETURNING id
)
INSERT INTO answers(question_id, answer) SELECT id, 'useRef' FROM q;

-- =======================
-- HARD (8 questions)
-- =======================

-- H1 UNIQUE_CHOICE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('UNIQUE_CHOICE', 2, 'HARD', 'What reconciler strategy helps React efficiently update the UI?')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('Diffing with heuristics', TRUE),
  ('Full DOM re-render on every change', FALSE),
  ('Manual DOM patching by the developer', FALSE),
  ('Shadow DOM from the browser', FALSE)
) AS v(txt, ok);

-- H2 TRUE_FALSE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('TRUE_FALSE', 2, 'HARD', 'useLayoutEffect runs after the browser paints the screen.')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES ('True', FALSE), ('False', TRUE)) AS v(txt, ok);

-- H3 MULTIPLE_CHOICE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('MULTIPLE_CHOICE', 2, 'HARD', 'Which patterns help avoid unnecessary re-renders?')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('React.memo', TRUE),
  ('useCallback', TRUE),
  ('Always inline arrow functions in JSX', FALSE),
  ('Mutating props before passing', FALSE)
) AS v(txt, ok);

-- H4 SHORT_ANSWER
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('SHORT_ANSWER', 2, 'HARD', 'Name the hook that memoizes a callback function.')
  RETURNING id
)
INSERT INTO answers(question_id, answer) SELECT id, 'useCallback' FROM q;

-- H5 UNIQUE_CHOICE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('UNIQUE_CHOICE', 2, 'HARD', 'Which scheduling mechanism can React use to prioritize rendering?')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('Cooperative scheduling', TRUE),
  ('Always synchronous blocking', FALSE),
  ('Global microtask queue only', FALSE),
  ('Mutation observers only', FALSE)
) AS v(txt, ok);

-- H6 TRUE_FALSE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('TRUE_FALSE', 2, 'HARD', 'Keys help React identify which items have changed in a list.')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES ('True', TRUE), ('False', FALSE)) AS v(txt, ok);

-- H7 MULTIPLE_CHOICE
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('MULTIPLE_CHOICE', 2, 'HARD', 'Pick techniques to prevent prop-drilling or manage state at scale.')
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES
  ('Context', TRUE),
  ('Redux or other state libs', TRUE),
  ('Mutating state in-place', FALSE),
  ('Keeping all state in the root only', FALSE)
) AS v(txt, ok);

-- H8 SHORT_ANSWER
WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question)
  VALUES ('SHORT_ANSWER', 2, 'HARD', 'Which hook memoizes a function and returns a stable reference across renders?')
  RETURNING id
)
INSERT INTO answers(question_id, answer) SELECT id, 'useCallback' FROM q;

-- =======================
-- Sanity checks (optional)
-- =======================
-- SELECT difficulty, type, COUNT(*) FROM questions WHERE category_id = 2 GROUP BY difficulty, type ORDER BY difficulty, type;
-- SELECT COUNT(*) FROM choices WHERE question_id IN (SELECT id FROM questions WHERE category_id = 2);
-- SELECT * FROM questions WHERE category_id = 2 ORDER BY id;

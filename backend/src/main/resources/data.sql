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
-- DELETE FROM open_answers WHERE question_id IN (SELECT id FROM questions WHERE category_id = 2);
-- DELETE FROM questions WHERE category_id = 2;
-- -----------------------------------------------------------------------

-- Ensure parent and child categories exist (idempotent)
INSERT INTO categories(name, slug, parent_id, created_at, updated_at)
VALUES ('Frontend', 'frontend', NULL, NOW(), NOW())
ON CONFLICT (slug)
    DO UPDATE SET name = EXCLUDED.name, updated_at = NOW();

INSERT INTO categories(name, slug, parent_id, created_at, updated_at)
VALUES ('React Hooks', 'frontend-react-hooks', (SELECT id FROM categories WHERE slug = 'frontend'), NOW(), NOW())
ON CONFLICT (slug)
    DO UPDATE SET name = EXCLUDED.name,
                  parent_id = (SELECT id FROM categories WHERE slug = 'frontend'),
                  updated_at = NOW();

-- =======================
-- EASY (8 questions)
-- =======================

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'EASY', 'What is React primarily used for?', NOW(), NOW())
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
                   (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'),
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
  VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'EASY', 'Which of the following are React features?', NOW(), NOW())
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
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'EASY', 'Which hook is used to manage state in a function component?', NOW(), NOW());

WITH q AS (
  SELECT id FROM questions WHERE question = 'Which hook is used to manage state in a function component?' AND category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks')
)
INSERT INTO open_answers(question_id, answer, min_score)
SELECT id, 'useState', 60 FROM q;

-- Additional comprehensive answer
WITH q AS (
  SELECT id FROM questions WHERE question = 'Which hook is used to manage state in a function component?' AND category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks')
)
INSERT INTO open_answers(question_id, answer, rubric_keywords, min_score)
SELECT id, 'useState is a React hook that allows functional components to have state by returning a state variable and a setter function.', '{"must": ["useState", "state"], "should": ["hook", "functional components", "setter function"]}', 70 FROM q;

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'EASY', 'JSX compiles down to calls to which function by default?', NOW(), NOW())
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
  VALUES ('TRUE_FALSE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'EASY', 'Props are mutable from inside the receiving component.', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES ('True', FALSE), ('False', TRUE)) AS v(txt, ok);

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'EASY', 'Pick valid places to use JSX expressions.', NOW(), NOW())
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
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'EASY', 'Name the hook used to access a mutable value that persists across renders without causing re-renders.', NOW(), NOW());

WITH q AS (
  SELECT id FROM questions WHERE question = 'Name the hook used to access a mutable value that persists across renders without causing re-renders.' AND category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks')
)
INSERT INTO open_answers(question_id, answer, min_score)
SELECT id, 'useRef', 60 FROM q;

-- Additional comprehensive answer
WITH q AS (
  SELECT id FROM questions WHERE question = 'Name the hook used to access a mutable value that persists across renders without causing re-renders.' AND category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks')
)
INSERT INTO open_answers(question_id, answer, rubric_keywords, min_score)
SELECT id, 'useRef creates a mutable ref object that persists across renders and does not trigger re-renders when its value changes, commonly used for DOM element access.', '{"must": ["useRef", "mutable", "persists"], "should": ["re-renders", "DOM element", "ref object"]}', 75 FROM q;

-- =======================
-- MEDIUM (8 questions)
-- =======================

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'MEDIUM', 'What prop must be supplied when rendering lists in React?', NOW(), NOW())
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
  VALUES ('TRUE_FALSE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'MEDIUM', 'useEffect runs synchronously before the browser paints.', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES ('True', FALSE), ('False', TRUE)) AS v(txt, ok);

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'MEDIUM', 'Which hooks trigger side effects or lifecycle-like behavior?', NOW(), NOW())
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
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'MEDIUM', 'What hook memoizes a computed value based on dependencies?', NOW(), NOW());

WITH q AS (
  SELECT id FROM questions WHERE question = 'What hook memoizes a computed value based on dependencies?' AND category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks')
)
INSERT INTO open_answers(question_id, answer, rubric_keywords, min_score)
SELECT id, 'useMemo', '{"must": ["useMemo", "memoization"], "should": ["dependencies", "performance", "optimization"]}', 65 FROM q;

-- Additional comprehensive answer
WITH q AS (
  SELECT id FROM questions WHERE question = 'What hook memoizes a computed value based on dependencies?' AND category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks')
)
INSERT INTO open_answers(question_id, answer, rubric_keywords, min_score)
SELECT id, 'useMemo is a React hook that memoizes expensive computations to avoid recalculating them on every render, only recalculating when dependencies change.', '{"must": ["useMemo", "memoization", "dependencies"], "should": ["performance", "expensive", "recalculate", "render"]}', 75 FROM q;

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'MEDIUM', 'Which pattern helps avoid unnecessary re-renders when passing callbacks?', NOW(), NOW())
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
  VALUES ('TRUE_FALSE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'MEDIUM', 'Context provides a way to pass data through the component tree without prop drilling.', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES ('True', TRUE), ('False', FALSE)) AS v(txt, ok);

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'MEDIUM', 'Which are valid ways to optimize expensive recalculations?', NOW(), NOW())
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
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'MEDIUM', 'Which hook is used to imperatively access a child component DOM node?', NOW(), NOW());

WITH q AS (
  SELECT id FROM questions WHERE question = 'Which hook is used to imperatively access a child component DOM node?' AND category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks')
)
INSERT INTO open_answers(question_id, answer, min_score)
SELECT id, 'useRef', 65 FROM q;

-- Additional comprehensive answer
WITH q AS (
  SELECT id FROM questions WHERE question = 'Which hook is used to imperatively access a child component DOM node?' AND category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks')
)
INSERT INTO open_answers(question_id, answer, rubric_keywords, min_score)
SELECT id, 'useRef hook creates a reference that can be attached to DOM elements, allowing imperative access to child component DOM nodes for operations like focus or scrolling.', '{"must": ["useRef", "DOM", "imperative"], "should": ["reference", "elements", "focus", "child component"]}', 75 FROM q;

-- =======================
-- HARD (8 questions)
-- =======================

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'HARD', 'What reconciler strategy helps React efficiently update the UI?', NOW(), NOW())
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
  VALUES ('TRUE_FALSE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'HARD', 'useLayoutEffect runs after the browser paints the screen.', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES ('True', FALSE), ('False', TRUE)) AS v(txt, ok);

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'HARD', 'Which patterns help avoid unnecessary re-renders?', NOW(), NOW())
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
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'HARD', 'Name the hook that memoizes a callback function.', NOW(), NOW());

WITH q AS (
  SELECT id FROM questions WHERE question = 'Name the hook that memoizes a callback function.' AND category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks')
)
INSERT INTO open_answers(question_id, answer, rubric_keywords, min_score)
SELECT id, 'useCallback', '{"must": ["useCallback", "memoization"], "should": ["callback", "function", "performance"]}', 70 FROM q;

-- Additional comprehensive answer
WITH q AS (
  SELECT id FROM questions WHERE question = 'Name the hook that memoizes a callback function.' AND category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks')
)
INSERT INTO open_answers(question_id, answer, rubric_keywords, min_score)
SELECT id, 'useCallback is a React hook that returns a memoized version of a callback function that only changes if one of its dependencies has changed, preventing unnecessary re-renders.', '{"must": ["useCallback", "memoized", "callback"], "should": ["dependencies", "re-renders", "performance", "function"]}', 80 FROM q;

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'HARD', 'Which scheduling mechanism can React use to prioritize rendering?', NOW(), NOW())
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
  VALUES ('TRUE_FALSE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'HARD', 'Keys help React identify which items have changed in a list.', NOW(), NOW())
  RETURNING id
)
INSERT INTO choices(question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok FROM q CROSS JOIN (VALUES ('True', TRUE), ('False', FALSE)) AS v(txt, ok);

WITH q AS (
  INSERT INTO questions(type, category_id, difficulty, question, created_at, updated_at)
  VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'HARD', 'Pick techniques to prevent prop-drilling or manage state at scale.', NOW(), NOW())
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
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'HARD', 'Which hook memoizes a function and returns a stable reference across renders?', NOW(), NOW());

WITH q AS (
  SELECT id FROM questions WHERE question = 'Which hook memoizes a function and returns a stable reference across renders?' AND category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks')
)
INSERT INTO open_answers(question_id, answer, rubric_keywords, min_score)
SELECT id, 'useCallback', '{"must": ["useCallback", "stable reference"], "should": ["memoizes", "renders", "function"]}', 70 FROM q;

-- Additional comprehensive answer
WITH q AS (
  SELECT id FROM questions WHERE question = 'Which hook memoizes a function and returns a stable reference across renders?' AND category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks')
)
INSERT INTO open_answers(question_id, answer, rubric_keywords, min_score)
SELECT id, 'useCallback hook memoizes functions and returns a stable reference that persists across renders unless dependencies change, helping to prevent child component re-renders.', '{"must": ["useCallback", "stable reference", "memoizes"], "should": ["dependencies", "re-renders", "child component", "persists"]}', 75 FROM q;

-- =======================
-- Sanity checks (optional)
-- =======================
-- SELECT difficulty, type, COUNT(*) FROM questions WHERE category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks') GROUP BY difficulty, type ORDER BY difficulty, type;
-- SELECT COUNT(*) FROM choices WHERE question_id IN (SELECT id FROM questions WHERE category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'));
-- SELECT COUNT(*) FROM open_answers WHERE question_id IN (SELECT id FROM questions WHERE category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'));
-- SELECT * FROM questions WHERE category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks') ORDER BY id;
-- SELECT oa.*, q.question FROM open_answers oa JOIN questions q ON oa.question_id = q.id WHERE q.category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks') ORDER BY oa.id;

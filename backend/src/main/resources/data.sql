-- =======================================================
-- Bell-Ringer — Flat Categories + Bulk Questions Seed
-- Compatible with the existing schema from data.sql
-- =======================================================

-- --------------------------------------
-- (Optional) DEV CLEANUP — use carefully
-- --------------------------------------
-- DELETE FROM attempt_answers;
-- DELETE FROM attempts;
-- DELETE FROM quiz_questions;
-- DELETE FROM choices WHERE question_id IN (SELECT id FROM questions);
-- DELETE FROM open_answers WHERE question_id IN (SELECT id FROM questions);
-- DELETE FROM questions;
-- DELETE FROM categories;

-- ==========================================
-- 1) Create Parent Categories (Areas)
-- ==========================================
INSERT INTO categories (name, slug, parent_id, created_at, updated_at)
VALUES ('Frontend', 'frontend', NULL, NOW(), NOW()),
       ('Backend', 'backend', NULL, NOW(), NOW()),
       ('Data', 'data', NULL, NOW(), NOW()),
       ('DevOps', 'devops', NULL, NOW(), NOW())
ON CONFLICT (slug) DO UPDATE
    SET name       = EXCLUDED.name,
        updated_at = NOW();

-- ==========================================
-- 2) Create Child Categories under Parents
-- ==========================================
INSERT INTO categories (name, slug, parent_id, created_at, updated_at)
VALUES
    -- Frontend children
    ('HTML & Accessibility', 'frontend-html-accessibility', (SELECT id FROM categories WHERE slug = 'frontend'), NOW(),
     NOW()),
    ('CSS Fundamentals', 'frontend-css-fundamentals', (SELECT id FROM categories WHERE slug = 'frontend'), NOW(),
     NOW()),
    ('TailwindCSS', 'frontend-tailwindcss', (SELECT id FROM categories WHERE slug = 'frontend'), NOW(), NOW()),
    ('JavaScript Basics', 'frontend-javascript-basics', (SELECT id FROM categories WHERE slug = 'frontend'), NOW(),
     NOW()),
    ('DOM Manipulation', 'frontend-dom-manipulation', (SELECT id FROM categories WHERE slug = 'frontend'), NOW(),
     NOW()),
    ('React Fundamentals', 'frontend-react-fundamentals', (SELECT id FROM categories WHERE slug = 'frontend'), NOW(),
     NOW()),
    ('React Hooks', 'frontend-react-hooks', (SELECT id FROM categories WHERE slug = 'frontend'), NOW(), NOW()),
    ('State Management', 'frontend-state-management', (SELECT id FROM categories WHERE slug = 'frontend'), NOW(),
     NOW()),

    -- Backend children
    ('Node.js Basics', 'backend-nodejs-basics', (SELECT id FROM categories WHERE slug = 'backend'), NOW(), NOW()),
    ('Express.js', 'backend-expressjs', (SELECT id FROM categories WHERE slug = 'backend'), NOW(), NOW()),
    ('Java Spring Boot', 'backend-java-spring-boot', (SELECT id FROM categories WHERE slug = 'backend'), NOW(), NOW()),
    ('API Design & REST', 'backend-api-design-rest', (SELECT id FROM categories WHERE slug = 'backend'), NOW(), NOW()),
    ('Authentication', 'backend-authentication', (SELECT id FROM categories WHERE slug = 'backend'), NOW(), NOW()),
    ('Python & Flask', 'backend-python-flask', (SELECT id FROM categories WHERE slug = 'backend'), NOW(), NOW()),

    -- Data children
    ('SQL Basics', 'data-sql-basics', (SELECT id FROM categories WHERE slug = 'data'), NOW(), NOW()),
    ('PostgreSQL Advanced', 'data-postgresql-advanced', (SELECT id FROM categories WHERE slug = 'data'), NOW(), NOW()),
    ('MongoDB Basics', 'data-mongodb-basics', (SELECT id FROM categories WHERE slug = 'data'), NOW(), NOW()),
    ('Data Modeling', 'data-modeling-principles', (SELECT id FROM categories WHERE slug = 'data'), NOW(), NOW()),

    -- DevOps children
    ('Git & GitHub', 'devops-git-github', (SELECT id FROM categories WHERE slug = 'devops'), NOW(), NOW()),
    ('Docker Fundamentals', 'devops-docker-fundamentals', (SELECT id FROM categories WHERE slug = 'devops'), NOW(),
     NOW()),
    ('CI/CD Basics', 'devops-cicd-basics', (SELECT id FROM categories WHERE slug = 'devops'), NOW(), NOW()),
    ('Cloud Deployment', 'devops-cloud-deployment', (SELECT id FROM categories WHERE slug = 'devops'), NOW(), NOW())
ON CONFLICT (slug) DO UPDATE
    SET name       = EXCLUDED.name,
        parent_id  = EXCLUDED.parent_id,
        updated_at = NOW();

-- =======================================================
-- 2) SEED QUESTIONS (mix of types & difficulties)
--    You can duplicate these blocks for more categories.
--    Categories included below:
--    - react-hooks
--    - python-flask
--    - frontend-javascript-basics
--    - data-sql-basics
--    - devops-git-github
--    - devops-docker-fundamentals
-- =======================================================

-- ==========================
-- Category: React Hooks
-- Slug: frontend-react-hooks
-- ==========================

-- EASY — TRUE_FALSE
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('TRUE_FALSE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'EASY',
                'useState returns an array with [state, setState].', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('True', TRUE), ('False', FALSE)) AS v(txt, ok);

-- EASY — UNIQUE_CHOICE
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'EASY',
                'Which hook is intended for side effects?', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('useEffect', TRUE),
                            ('useMemo', FALSE),
                            ('useId', FALSE),
                            ('useState', FALSE)) AS v(txt, ok);

-- MEDIUM — MULTIPLE_CHOICE
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'MEDIUM',
                'Which help prevent unnecessary re-renders?', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('React.memo', TRUE),
                            ('useCallback', TRUE),
                            ('Mutating props', FALSE),
                            ('Always inline new functions', FALSE)) AS v(txt, ok);

-- HARD — SHORT_ANSWER
INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'HARD',
        'Name the hook that memoizes a function and returns a stable reference.', NOW(), NOW());

WITH q AS (SELECT id
           FROM questions
           WHERE question = 'Name the hook that memoizes a function and returns a stable reference.'
             AND category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'))
INSERT
INTO open_answers (question_id, answer, rubric_keywords, min_score)
SELECT id, 'useCallback', '{"must":["useCallback"],"should":["stable reference","memoize","dependencies"]}', 75
FROM q;

-- ADDITIONAL — SHORT_ANSWER (EASY)
INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'EASY',
        'What dependency array makes useEffect run only once after the initial render?', NOW(), NOW());

WITH q AS (SELECT id
           FROM questions
           WHERE question = 'What dependency array makes useEffect run only once after the initial render?'
             AND category_id = (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'))
INSERT
INTO open_answers (question_id, answer, rubric_keywords, min_score)
SELECT id, '[]', '{"must":["[]"],"should":["empty dependency array","mount"]}', 70
FROM q;

-- ADDITIONAL — UNIQUE_CHOICE (MEDIUM)
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-react-hooks'), 'MEDIUM',
                'Which hook memoizes a computed value across renders?', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('useMemo', TRUE),
                            ('useEffect', FALSE),
                            ('useRef', FALSE),
                            ('useTransition', FALSE)) AS v(txt, ok);
-- ==========================
-- Category: Python & Flask
-- Slug: backend-python-flask
-- ==========================

-- EASY — UNIQUE_CHOICE
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'backend-python-flask'), 'EASY',
                'Which command runs a Flask app with auto-reload (Flask ≥2)?', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('flask run --debug', TRUE),
                            ('flask serve', FALSE),
                            ('python app.py --dev', FALSE),
                            ('flask dev', FALSE)) AS v(txt, ok);

-- MEDIUM — TRUE_FALSE
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('TRUE_FALSE', (SELECT id FROM categories WHERE slug = 'backend-python-flask'), 'MEDIUM',
                'Blueprints help modularize routes and assets.', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('True', TRUE), ('False', FALSE)) AS v(txt, ok);

-- MEDIUM — SHORT_ANSWER
INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'backend-python-flask'), 'MEDIUM',
        'Name a common Flask DB integration extension.', NOW(), NOW());

WITH q AS (SELECT id
           FROM questions
           WHERE question = 'Name a common Flask DB integration extension.'
             AND category_id = (SELECT id FROM categories WHERE slug = 'backend-python-flask'))
INSERT
INTO open_answers (question_id, answer, rubric_keywords, min_score)
SELECT id, 'Flask SQLAlchemy', '{"must":["SQLAlchemy"],"should":["ORM","flask_sqlalchemy"]}', 70
FROM q;

-- HARD — MULTIPLE_CHOICE
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'backend-python-flask'), 'HARD',
                'Secure practices for secrets/config in Flask include:', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('Environment variables / secrets manager', TRUE),
                            ('Hard-coding secrets in views', FALSE),
                            ('app.config.from_envvar / from_mapping safely', TRUE),
                            ('Committing .env with real creds', FALSE)) AS v(txt, ok);

-- ADDITIONAL — SHORT_ANSWER (EASY)
INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'backend-python-flask'), 'EASY',
        'Which Flask object do you use to access incoming HTTP data (query params, form, JSON)?', NOW(), NOW());

WITH q AS (SELECT id
           FROM questions
           WHERE question = 'Which Flask object do you use to access incoming HTTP data (query params, form, JSON)?'
             AND category_id = (SELECT id FROM categories WHERE slug = 'backend-python-flask'))
INSERT
INTO open_answers (question_id, answer, rubric_keywords, min_score)
SELECT id, 'request', '{"must":["request"],"should":["flask.request","args","form","json"]}', 60
FROM q;

-- ==========================
-- Category: JavaScript Basics
-- Slug: frontend-javascript-basics
-- ==========================

-- EASY — TRUE_FALSE
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('TRUE_FALSE', (SELECT id FROM categories WHERE slug = 'frontend-javascript-basics'), 'EASY',
                '`const` creates a block-scoped binding.', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('True', TRUE), ('False', FALSE)) AS v(txt, ok);

-- MEDIUM — UNIQUE_CHOICE
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-javascript-basics'), 'MEDIUM',
                'Which operator checks both value and type equality?', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('===', TRUE), ('==', FALSE), ('=', FALSE), ('!==', FALSE)) AS v(txt, ok);

-- HARD — MULTIPLE_CHOICE
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-javascript-basics'), 'HARD',
                'Which are falsy in JavaScript?', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('0', TRUE),
                            ('''', TRUE),
                            ('undefined', TRUE),
                            ('[]', FALSE)) AS v(txt, ok);

-- ADDITIONAL — UNIQUE_CHOICE (EASY)
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'frontend-javascript-basics'), 'EASY',
                'Which keyword declares a constant binding?', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('const', TRUE), ('let', FALSE), ('var', FALSE), ('static', FALSE)) AS v(txt, ok);

-- ADDITIONAL — SHORT_ANSWER (MEDIUM)
INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'frontend-javascript-basics'), 'MEDIUM',
        'What is the result of typeof NaN in JavaScript?', NOW(), NOW());

WITH q AS (SELECT id
           FROM questions
           WHERE question = 'What is the result of typeof NaN in JavaScript?'
             AND category_id = (SELECT id FROM categories WHERE slug = 'frontend-javascript-basics'))
INSERT
INTO open_answers (question_id, answer, rubric_keywords, min_score)
SELECT id, 'number', '{"must":["number"],"should":["typeof","NaN"]}', 70
FROM q;

-- ==========================
-- Category: SQL Basics
-- Slug: data-sql-basics
-- ==========================

-- EASY — UNIQUE_CHOICE
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'data-sql-basics'), 'EASY',
                'Which keyword removes duplicate rows in a SELECT?', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('DISTINCT', TRUE),
                            ('GROUP BY', FALSE),
                            ('UNIQUE', FALSE),
                            ('FILTER', FALSE)) AS v(txt, ok);

-- MEDIUM — SHORT_ANSWER
INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'data-sql-basics'), 'MEDIUM',
        'Write a WHERE clause to filter rows where price is greater than 100.', NOW(), NOW());

WITH q AS (SELECT id
           FROM questions
           WHERE question = 'Write a WHERE clause to filter rows where price is greater than 100.'
             AND category_id = (SELECT id FROM categories WHERE slug = 'data-sql-basics'))
INSERT
INTO open_answers (question_id, answer, rubric_keywords, min_score)
SELECT id, 'WHERE price > 100', '{"must":["price > 100"],"should":["WHERE"]}', 70
FROM q;

-- HARD — MULTIPLE_CHOICE
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'data-sql-basics'), 'HARD',
                'Which statements about indexes are true?', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('Indexes can speed up reads', TRUE),
                            ('Indexes always speed up writes', FALSE),
                            ('A bad index can hurt performance', TRUE),
                            ('Indexes are ignored in JOINs', FALSE)) AS v(txt, ok);

-- ADDITIONAL — TRUE_FALSE (EASY)
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('TRUE_FALSE', (SELECT id FROM categories WHERE slug = 'data-sql-basics'), 'EASY',
                'In PostgreSQL, a PRIMARY KEY implies both UNIQUE and NOT NULL.', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('True', TRUE), ('False', FALSE)) AS v(txt, ok);

-- ADDITIONAL — UNIQUE_CHOICE (MEDIUM)
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'data-sql-basics'), 'MEDIUM',
                'Which clause filters aggregated groups rather than individual rows?', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('HAVING', TRUE), ('WHERE', FALSE), ('GROUP BY', FALSE), ('ORDER BY', FALSE)) AS v(txt, ok);


-- ==========================
-- Category: Git & GitHub
-- Slug: devops-git-github
-- ==========================

-- EASY — TRUE_FALSE
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('TRUE_FALSE', (SELECT id FROM categories WHERE slug = 'devops-git-github'), 'EASY',
                'A fast-forward merge moves the branch pointer without a new merge commit.', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('True', TRUE), ('False', FALSE)) AS v(txt, ok);

-- MEDIUM — UNIQUE_CHOICE
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'devops-git-github'), 'MEDIUM',
                'Which command stages *all* modified and deleted files?', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('git add -A', TRUE),
                            ('git add .', FALSE),
                            ('git stage *', FALSE),
                            ('git commit -a', FALSE)) AS v(txt, ok);

-- HARD — SHORT_ANSWER
INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'devops-git-github'), 'HARD',
        'What option prevents fast-forward on merge to force a merge commit?', NOW(), NOW());

WITH q AS (SELECT id
           FROM questions
           WHERE question = 'What option prevents fast-forward on merge to force a merge commit?'
             AND category_id = (SELECT id FROM categories WHERE slug = 'devops-git-github'))
INSERT
INTO open_answers (question_id, answer, rubric_keywords, min_score)
SELECT id, '--no-ff', '{"must":["--no-ff"],"should":["merge"]}', 70
FROM q;

-- ADDITIONAL — UNIQUE_CHOICE (EASY)
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'devops-git-github'), 'EASY',
                'Which command creates a new branch and switches to it (modern Git)?', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('git switch -c feature', TRUE),
                            ('git checkout feature', FALSE),
                            ('git branch feature && git reset', FALSE),
                            ('git merge -c feature', FALSE)) AS v(txt, ok);

-- ADDITIONAL — MULTIPLE_CHOICE (MEDIUM)
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'devops-git-github'), 'MEDIUM',
                'Which commands can rewrite Git history?', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('git rebase', TRUE),
                            ('git commit --amend', TRUE),
                            ('git reset --hard', TRUE),
                            ('git merge --no-ff', FALSE)) AS v(txt, ok);

-- ==========================
-- Category: Docker Fundamentals
-- Slug: devops-docker-fundamentals
-- ==========================

-- EASY — TRUE_FALSE
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('TRUE_FALSE', (SELECT id FROM categories WHERE slug = 'devops-docker-fundamentals'), 'EASY',
                'A Docker image is an immutable snapshot used to create containers.', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('True', TRUE), ('False', FALSE)) AS v(txt, ok);

-- MEDIUM — UNIQUE_CHOICE
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'devops-docker-fundamentals'), 'MEDIUM',
                'Which command builds an image from a Dockerfile in the current directory?', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('docker build .', TRUE),
                            ('docker make .', FALSE),
                            ('docker image create .', FALSE),
                            ('docker compose up --build', FALSE)) AS v(txt, ok);

-- HARD — MULTIPLE_CHOICE
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('MULTIPLE_CHOICE', (SELECT id FROM categories WHERE slug = 'devops-docker-fundamentals'), 'HARD',
                'Good practices for small, secure images include:', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('Use slim/alpine base images when appropriate', TRUE),
                            ('Copy only required artifacts', TRUE),
                            ('Run as root in production images', FALSE),
                            ('Bake secrets into the image', FALSE)) AS v(txt, ok);

-- ADDITIONAL — UNIQUE_CHOICE (EASY)
WITH q AS (
    INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
        VALUES ('UNIQUE_CHOICE', (SELECT id FROM categories WHERE slug = 'devops-docker-fundamentals'), 'EASY',
                'Which Dockerfile instruction begins a new build stage?', NOW(), NOW())
        RETURNING id)
INSERT
INTO choices (question_id, choice_text, is_correct)
SELECT id, v.txt, v.ok
FROM q
         CROSS JOIN (VALUES ('FROM', TRUE), ('STAGE', FALSE), ('AS', FALSE), ('RUN', FALSE)) AS v(txt, ok);

-- ADDITIONAL — SHORT_ANSWER (MEDIUM)
INSERT INTO questions (type, category_id, difficulty, question, created_at, updated_at)
VALUES ('SHORT_ANSWER', (SELECT id FROM categories WHERE slug = 'devops-docker-fundamentals'), 'MEDIUM',
        'Write the Docker CLI command to list only running containers.', NOW(), NOW());

WITH q AS (SELECT id
           FROM questions
           WHERE question = 'Write the Docker CLI command to list only running containers.'
             AND category_id = (SELECT id FROM categories WHERE slug = 'devops-docker-fundamentals'))
INSERT
INTO open_answers (question_id, answer, rubric_keywords, min_score)
SELECT id, 'docker ps', '{"must":["docker ps"],"should":["list","running containers"]}', 65
FROM q;

-- =======================
-- Optional sanity checks
-- =======================
SELECT c.slug, q.difficulty, q.type, COUNT(*) AS cnt
FROM questions q
         JOIN categories c ON c.id = q.category_id
WHERE c.slug IN ('frontend-react-hooks', 'backend-python-flask', 'frontend-javascript-basics', 'data-sql-basics',
                 'devops-git-github', 'devops-docker-fundamentals')
GROUP BY c.slug, q.difficulty, q.type
ORDER BY c.slug, q.difficulty, q.type;
-- ============================================
-- FarmAI Password Fix Script
-- ============================================
-- 
-- IMPORTANT: This SQL file is for REFERENCE ONLY.
-- The passwords in the database must be generated
-- using the Java PasswordUtil.hashPassword() method.
-- 
-- The PasswordFixer utility will automatically fix
-- passwords when you run:
--   1. DatabaseInitializer (before app starts)
--   2. Or manually: java tn.esprit.farmai.database.PasswordFixer
--
-- Why can't we use SQL directly?
-- Passwords use SHA-256 with a random salt.
-- Each hash is unique because of the random salt.
-- The format is: base64(salt) + "$" + base64(hash)
--
-- Run the Java fixer instead:
--   mvn exec:java -Dexec.mainClass="tn.esprit.farmai.database.PasswordFixer"
-- 
-- Or simply start the application - it fixes passwords automatically!
-- ============================================

-- This query shows the password format (should have $ separator)
SELECT id_user, email, 
       CASE 
         WHEN password LIKE '%$%' THEN 'Correct format (salt$hash)'
         ELSE 'WRONG FORMAT - needs fixing'
       END as password_status,
       LEFT(password, 20) as password_preview
FROM user;

-- After running PasswordFixer, verify with this query:
-- SELECT email, LEFT(password, 50) as hashed_password FROM user WHERE email = 'agricole@farmai.tn';
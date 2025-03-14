---------------------------------
-- Data for H2
---------------------------------
INSERT INTO user_table (username, email, password, role) VALUES
('john_doe', 'john@example.com', 'hashedpassword123', 'USER'),
('admin_user', 'admin@example.com', 'securepassword456', 'ADMIN'),
('mod_guy', 'moderator@example.com', 'modpassword789', 'MODERATOR');

INSERT INTO user_reviews_ids (reviews_ids, user_username) VALUES
(101, 'john_doe'),
(102, 'john_doe'),
(201, 'admin_user'),
(301, 'mod_guy'),
(302, 'mod_guy'),
(303, 'mod_guy');

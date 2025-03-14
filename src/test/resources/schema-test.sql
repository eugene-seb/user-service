---------------------------------
-- Database configuration (H2)
---------------------------------
CREATE TABLE public.user_reviews_ids (
    reviews_ids integer,
    user_username VARCHAR(255) NOT NULL
);

CREATE TABLE public.user_table (
    email VARCHAR(255),
    password VARCHAR(255),
    role VARCHAR(255),
    username VARCHAR(255) NOT NULL,
    CONSTRAINT user_table_role_check CHECK (role IN ('USER', 'ADMIN', 'MODERATOR'))
);

ALTER TABLE public.user_table ADD CONSTRAINT user_table_pkey PRIMARY KEY (username);

ALTER TABLE public.user_reviews_ids ADD CONSTRAINT fks2q6th7ign1mdfaae2ocd9av8
    FOREIGN KEY (user_username) REFERENCES public.user_table(username);

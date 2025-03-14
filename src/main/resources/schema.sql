--
-- PostgreSQL database dump
--

-- Dumped from database version 17.4
-- Dumped by pg_dump version 17.4

-- Started on 2025-03-13 19:52:01

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 217 (class 1259 OID 24978)
-- Name: user_reviews_ids; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_reviews_ids (
    reviews_ids integer,
    user_username character varying(255) NOT NULL
);


ALTER TABLE public.user_reviews_ids OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 24981)
-- Name: user_table; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_table (
    email character varying(255),
    password character varying(255),
    role character varying(255),
    username character varying(255) NOT NULL,
    CONSTRAINT user_table_role_check CHECK (((role)::text = ANY ((ARRAY['USER'::character varying, 'ADMIN'::character varying, 'MODERATOR'::character varying])::text[])))
);


ALTER TABLE public.user_table OWNER TO postgres;

--
-- TOC entry 4700 (class 2606 OID 24988)
-- Name: user_table user_table_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_table
    ADD CONSTRAINT user_table_pkey PRIMARY KEY (username);


--
-- TOC entry 4701 (class 2606 OID 24989)
-- Name: user_reviews_ids fks2q6th7ign1mdfaae2ocd9av8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_reviews_ids
    ADD CONSTRAINT fks2q6th7ign1mdfaae2ocd9av8 FOREIGN KEY (user_username) REFERENCES public.user_table(username);


-- Completed on 2025-03-13 19:52:01

--
-- PostgreSQL database dump complete
--


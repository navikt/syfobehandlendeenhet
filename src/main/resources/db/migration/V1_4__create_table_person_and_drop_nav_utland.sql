CREATE TABLE PERSON
(
    id            SERIAL PRIMARY KEY,
    uuid          VARCHAR(50) NOT NULL UNIQUE,
    personident   VARCHAR(11) NOT NULL UNIQUE,
    is_nav_utland BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    timestamptz NOT NULL,
    updated_at    timestamptz NOT NULL
);

GRANT SELECT ON PERSON TO cloudsqliamuser;
GRANT SELECT ON PERSON TO "isyfo-analyse";

DROP TABLE NAV_UTLAND;

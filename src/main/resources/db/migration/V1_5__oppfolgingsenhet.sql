ALTER TABLE PERSON ADD COLUMN oppfolgingsenhet CHAR(4);

UPDATE PERSON SET oppfolgingsenhet='0393' WHERE is_nav_utland;

ALTER TABLE PERSON DROP COLUMN is_nav_utland;

CREATE INDEX IX_PERSON_PERSONIDENT on PERSON (personident);

CREATE TABLE PERSON_HISTORIKK
(
    id               SERIAL PRIMARY KEY,
    uuid             VARCHAR(36) NOT NULL UNIQUE,
    personident      VARCHAR(11) NOT NULL,
    oppfolgingsenhet CHAR(4) NOT NULL,
    fom              DATE NOT NULL,
    tom              DATE NOT NULL
);

CREATE INDEX IX_PERSON_HISTORIKK_PERSONIDENT on PERSON_HISTORIKK (personident);

GRANT SELECT ON PERSON_HISTORIKK TO cloudsqliamuser;
GRANT SELECT ON PERSON_HISTORIKK TO "isyfo-analyse";

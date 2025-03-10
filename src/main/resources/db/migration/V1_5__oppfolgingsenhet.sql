CREATE TABLE OPPFOLGINGSENHET
(
    id                  SERIAL PRIMARY KEY,
    uuid                VARCHAR(36) NOT NULL UNIQUE,
    personident         VARCHAR(11) NOT NULL,
    oppfolgingsenhet    CHAR(4),
    veilederident       VARCHAR(7) NOT NULL,
    created_at          timestamptz NOT NULL
);

GRANT SELECT ON OPPFOLGINGSENHET TO cloudsqliamuser;
GRANT SELECT ON OPPFOLGINGSENHET TO "isyfo-analyse";

INSERT INTO OPPFOLGINGSENHET (uuid, personident, oppfolgingsenhet, veilederident, created_at)
SELECT uuid, personident, '0393', 'Z999999', updated_at FROM PERSON WHERE is_nav_utland;

CREATE INDEX IX_OPPFOLGINGSENHET_PERSONIDENT on OPPFOLGINGSENHET (personident, created_at);

DROP TABLE PERSON;

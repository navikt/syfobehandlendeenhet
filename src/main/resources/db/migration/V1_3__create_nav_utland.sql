CREATE TABLE NAV_UTLAND
(
    id            SERIAL PRIMARY KEY,
    uuid          VARCHAR(50) NOT NULL UNIQUE,
    personident   VARCHAR(11) NOT NULL UNIQUE,
    is_nav_utland BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    timestamptz NOT NULL,
    updated_at    timestamptz NOT NULL
);

GRANT SELECT ON NAV_UTLAND TO cloudsqliamuser;
GRANT SELECT ON NAV_UTLAND TO "isyfo-analyse";

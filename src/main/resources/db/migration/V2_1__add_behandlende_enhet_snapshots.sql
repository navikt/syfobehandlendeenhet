ALTER TABLE OPPFOLGINGSENHET
    ADD COLUMN behandlende_enhet_fra CHAR(4),
    ADD COLUMN behandlende_enhet_til CHAR(4);

COMMENT ON COLUMN OPPFOLGINGSENHET.behandlende_enhet_fra IS 'Effective unit before the update, as recorded at update time';
COMMENT ON COLUMN OPPFOLGINGSENHET.behandlende_enhet_til IS 'Effective unit after the update, as recorded at update time';

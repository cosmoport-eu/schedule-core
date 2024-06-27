--
-- Initial core schema
--
-- @encoding UTF-8
--

--
-- Translation (L18N)
--
-- The reference id of the translated value to use in tables
CREATE TABLE I18N
(
    id          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    tag         TEXT    NOT NULL DEFAULT (''),
    -- A flag of the translation which shows that the value is used externally
    external    BOOLEAN NOT NULL DEFAULT (0),
    description TEXT    NOT NULL DEFAULT (''),
    params      TEXT    NOT NULL DEFAULT (''),

    CONSTRAINT unique_tag UNIQUE (tag)
);
-- Language locales (EN, RU, DE, etc.)
CREATE TABLE LOCALE
(
    id                 INTEGER    NOT NULL PRIMARY KEY,
    code               VARCHAR(6) NOT NULL,
    -- The default locale flag for all values which not set
    is_default         BOOLEAN    NOT NULL DEFAULT (0),
    locale_description TEXT       NOT NULL DEFAULT (''),
    show               BOOLEAN    NOT NULL DEFAULT (0),
    show_time          INTEGER    NOT NULL DEFAULT (1),

    CONSTRAINT unique_code UNIQUE (code)
);
-- The main translation table of translated values to use internally and externally
CREATE TABLE TRANSLATION
(
    id        INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    i18n_id   INTEGER NOT NULL,
    locale_id INTEGER NOT NULL,
    tr_text   TEXT    NOT NULL DEFAULT (''),

    CONSTRAINT unique_i18n_locale UNIQUE (i18n_id, locale_id),

    FOREIGN KEY (i18n_id) REFERENCES I18N (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (locale_id) REFERENCES LOCALE (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

--
-- Stores scheduled events of the timetable
--
-- The type of the event
CREATE TABLE EVENT_TYPE
(
    id                          INTEGER        NOT NULL PRIMARY KEY AUTOINCREMENT,
    category_id                 INTEGER,
    i18n_event_type_name        INTEGER        NOT NULL,
    i18n_event_type_description INTEGER,
    default_duration            INTEGER        NOT NULL DEFAULT (1),
    default_repeat_interval     INTEGER        NOT NULL DEFAULT (0),
    default_cost                DECIMAL(10, 2) NOT NULL DEFAULT (0.00),

    CONSTRAINT unique_event_name_for_category UNIQUE (category_id, i18n_event_type_name),
    CONSTRAINT not_empty_type_name CHECK (i18n_event_type_name <> ''),

    FOREIGN KEY (category_id) REFERENCES EVENT_TYPE_CATEGORY (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    FOREIGN KEY (i18n_event_type_name) REFERENCES I18N (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    FOREIGN KEY (i18n_event_type_description) REFERENCES I18N (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);
--
-- Stores multilevel event category hierarchy
--
CREATE TABLE EVENT_TYPE_CATEGORY
(
    id                            INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    i18n_event_type_category_name INTEGER,
    parent                        INTEGER,

    FOREIGN KEY (i18n_event_type_category_name) REFERENCES I18N (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    FOREIGN KEY (parent) REFERENCES EVENT_TYPE_CATEGORY (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);
-- The event status (to display)
CREATE TABLE EVENT_STATUS
(
    id          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    i18n_status INTEGER,

    FOREIGN KEY (i18n_status) REFERENCES I18N (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);
-- The event state (for internal use mostly)
CREATE TABLE EVENT_STATE
(
    id         INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    i18n_state INTEGER,

    FOREIGN KEY (i18n_state) REFERENCES I18N (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);
-- The point of destination of the travel event if present
CREATE TABLE EVENT_DESTINATION
(
    id                          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    i18n_event_destination_name INTEGER,

    FOREIGN KEY (i18n_event_destination_name) REFERENCES I18N (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);
-- The gates for travel events if present
CREATE TABLE GATE
(
    id        INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    number    INTEGER NOT NULL,
    gate_name TEXT    NOT NULL DEFAULT ('')
);
-- Events' schedule by days
CREATE TABLE TIMETABLE
(
    id                   INTEGER        NOT NULL PRIMARY KEY AUTOINCREMENT,
    event_date           DATE           NOT NULL,
    event_type_id        INTEGER,
    event_status_id      INTEGER,
    event_state_id       INTEGER        NOT NULL DEFAULT (1),
    event_destination_id INTEGER,
    gate_id              INTEGER,
    gate2_id             INTEGER,
    start_time           INTEGER        NOT NULL DEFAULT (0),
    duration_time        INTEGER        NOT NULL DEFAULT (1),
    repeat_interval      INTEGER        NOT NULL DEFAULT (0),
    cost                 DECIMAL(10, 2) NOT NULL DEFAULT (0.00),
    people_limit         INTEGER        NOT NULL DEFAULT (1),
    contestants          INTEGER        NOT NULL DEFAULT (0),
    date_added           TIMESTAMP      NOT NULL DEFAULT (CURRENT_TIMESTAMP),

    CONSTRAINT check_people_limit CHECK (contestants <= people_limit),
    CONSTRAINT check_minutes_in_day_limit CHECK (start_time < 1441 AND duration_time < 1441),
    CONSTRAINT check_event_duration_less_a_day CHECK (start_time + duration_time <= 1440),

    FOREIGN KEY (event_type_id) REFERENCES EVENT_TYPE (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    FOREIGN KEY (event_status_id) REFERENCES EVENT_STATUS (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    FOREIGN KEY (event_state_id) REFERENCES EVENT_STATE (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    FOREIGN KEY (event_destination_id) REFERENCES EVENT_DESTINATION (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    FOREIGN KEY (gate_id) REFERENCES GATE (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    FOREIGN KEY (gate2_id) REFERENCES GATE (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

-- Triggers for EVENTS
--
-- Delete all i18n links (reverse foreign key)
---
CREATE TRIGGER EVENT_TYPE_AD
    AFTER
        DELETE
    ON EVENT_TYPE
    FOR EACH ROW
BEGIN
    DELETE FROM I18N WHERE id = old.i18n_event_type_name;
    DELETE FROM I18N WHERE id = old.i18n_event_type_description;
    DELETE FROM TRANSLATION WHERE i18n_id = old.i18n_event_type_name;
    DELETE FROM TRANSLATION WHERE i18n_id = old.i18n_event_type_description;

    -- clear the empty subcategory if there are no more events
    DELETE
    FROM EVENT_TYPE_CATEGORY
    WHERE old.category_id = id
      AND NOT EXISTS(SELECT 1 FROM EVENT_TYPE WHERE category_id = old.category_id);
END;

CREATE TRIGGER EVENT_TYPE_CATEGORY_AD
    AFTER
        DELETE
    ON EVENT_TYPE_CATEGORY
    FOR EACH ROW
BEGIN
    DELETE FROM I18N WHERE id = old.i18n_event_type_category_name;
    DELETE FROM TRANSLATION WHERE i18n_id = old.i18n_event_type_category_name;
END;

--
-- Triggers for TIMETABLE
--
-- State changing
--
-- On tickets increase
--
CREATE TRIGGER TIMETABLE_STATE_CHANGE_ON_TICKETS_INCREASE_AI
    AFTER
        INSERT
    ON TIMETABLE
    WHEN NEW.contestants = NEW.people_limit
BEGIN
    UPDATE TIMETABLE
    SET event_state_id = 2
    WHERE id = NEW.id;
END;

CREATE TRIGGER TIMETABLE_STATE_CHANGE_ON_TICKETS_INCREASE_AU
    AFTER
        UPDATE
    ON TIMETABLE
    WHEN NEW.contestants = NEW.people_limit
BEGIN
    UPDATE TIMETABLE
    SET event_state_id = 2
    WHERE id = NEW.id;
END;

--
-- Not allow to buy if closed
--
CREATE TRIGGER TIMETABLE_RESTRICT_BUY
    BEFORE
        UPDATE
    ON TIMETABLE
    WHEN NEW.event_state_id = 2 AND NEW.contestants <> OLD.contestants
BEGIN
    SELECT RAISE(ABORT, 'Cannot sell tickets if closed (you should `reopen` the event first).');
END;

CREATE TABLE SETTINGS
(
    id    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    param TEXT    NOT NULL DEFAULT (''),
    value TEXT    NOT NULL DEFAULT (''),

    CONSTRAINT unique_key UNIQUE (param),
    CONSTRAINT check_not_empty_key CHECK (param <> '')
);

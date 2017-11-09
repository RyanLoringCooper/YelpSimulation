SET define OFF;
/* Types */
CREATE OR REPLACE TYPE hours_type as OBJECT (
    day             VARCHAR(16),
    open            VARCHAR(16),
    close           VARCHAR(16)
);
/

CREATE OR REPLACE TYPE attribute_type as OBJECT (
    attr            VARCHAR(64),
    value           VARCHAR(64)
);
/

CREATE OR REPLACE TYPE votes_type as OBJECT (
    funny           INTEGER,
    useful          INTEGER,
    cool            INTEGER
);
/

CREATE OR REPLACE TYPE hoursTable AS TABLE OF hours_type;
/

CREATE OR REPLACE TYPE attributeTable AS TABLE OF attribute_type;
/

CREATE OR REPLACE TYPE neighborhoodTable AS TABLE OF VARCHAR(512);
/

CREATE OR REPLACE TYPE eliteTable AS TABLE OF INTEGER;
/

/* Entities */
CREATE TABLE Business (
    business_id             VARCHAR(128) PRIMARY KEY,
    full_address            VARCHAR(256) NOT NULL,
    hours                   hoursTable,
    open                    VARCHAR(8), /* true or false */
    city                    VARCHAR(64) NOT NULL,
    review_count            INTEGER DEFAULT 0,
    name                    VARCHAR(64) NOT NULL,
    neighborhoods           neighborhoodTable,
    longitude               NUMBER NOT NULL,
    state                   VARCHAR(8) NOT NULL,
    stars                   NUMBER DEFAULT 0,
    latitude                NUMBER NOT NULL,
    attributes              attributeTable
) 
NESTED TABLE hours STORE AS businessHoursTable
NESTED TABLE attributes STORE AS businessAttributesTable
NESTED TABLE neighborhoods STORE AS businessNeighborhoodsTable;

CREATE TABLE Category (
    id                      INTEGER PRIMARY KEY,
    name                    VARCHAR(128), 
    business                VARCHAR(128), 
    CONSTRAINT cbid FOREIGN KEY (business) REFERENCES Business(business_id)
); 

CREATE TABLE YelpUser ( /* compliments and friends are missing*/
    yelping_since           VARCHAR(16) NOT NULL,
    votes                   votes_type,
    review_count            INTEGER DEFAULT 0,
    name                    VARCHAR(32) NOT NULL,
    user_id                 VARCHAR(128) PRIMARY KEY,
    fans                    INTEGER DEFAULT 0,
    average_stars           NUMBER DEFAULT 0,
    elite                   eliteTable
)
NESTED TABLE elite STORE AS yelpUserEliteTable;

CREATE TABLE Review (
    votes                   votes_type,
    user_id                 VARCHAR(128) NOT NULL,
    review_id               VARCHAR(128) PRIMARY KEY,
    stars                   INTEGER DEFAULT 0,
    date_field              DATE NOT NULL,
    text                    CLOB NOT NULL,
    business_id             VARCHAR(128) NOT NULL,
    CONSTRAINT r_bid FOREIGN KEY (business_id) REFERENCES Business(business_id),
    CONSTRAINT r_uid FOREIGN KEY (user_id) REFERENCES YelpUser(user_id)
);

/* Functions */

CREATE OR REPLACE FUNCTION appendToText ( textToAppend CLOB, targetReview VARCHAR ) 
    RETURN NUMBER AS
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
    UPDATE Review r
    SET r.text = r.text || textToAppend
    WHERE r.review_id = targetReview;
    COMMIT;
    RETURN 0;
END;
/

/* Indexes */
CREATE INDEX reviewIndex ON Review(business_id);

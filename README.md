N2 DMS
==========

### Build

    $ mvn -Dmaven.test.skip=true clean gwt:compile install

### Migration

#### I18n Translations

    $ mv ./src/main/resources/i18n/ja-JP.sql ./src/main/resources/i18n/ja-JP_OLD.sql
    $ bash ./migrate_translation.sh ./src/main/resources/i18n/en-GB.sql ./src/main/resources/i18n/ja-JP_OLD.sql > ./src/main/resources/i18n/ja-JP.sql


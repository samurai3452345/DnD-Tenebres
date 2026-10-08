# Database migration policy

PostgreSQL is the authoritative database for DnD Tenebres. H2 is used only for fast unit and
application-context tests; it is not accepted as proof that a Liquibase migration is valid.

## Immutable history

- A changeset that has run in any shared environment is never edited, reordered, or deleted.
- Corrections are appended as a new, uniquely numbered migration and included at the end of
  `db.changelog-master.xml`.
- `validCheckSum` is not added to new changesets. The existing exceptions in migrations 001,
  004, and 006 predate this policy and remain documented compatibility exceptions.
- A checksum mismatch is investigated. It is never bypassed with `ANY` or an extra checksum
  unless the exact historical file and deployment are documented in an incident record.

## Required verification

Every migration pull request must run `PostgresIntegrationTest` against PostgreSQL through
Testcontainers. CI treats an unavailable Docker runtime as a failure. The test starts from an
empty database and lets Liquibase apply the full history before Hibernate validates mappings.

For a release candidate, also restore the latest production backup into a disposable database,
apply the pending migrations, and run smoke tests. This catches upgrade-path problems that an
empty database cannot expose.

## Rollback and recovery

New structural migrations must contain an explicit Liquibase rollback. Before deploying a
migration that drops a column, rewrites rows, changes a foreign-key delete action, or adds a
constraint to existing data:

1. take and verify a restorable PostgreSQL backup;
2. run the forward migration on a restored copy;
3. run its rollback on that copy;
4. reapply it and execute application smoke tests.

Rollback must stop instead of deleting historical data to satisfy an old constraint. In
particular, rolling back migration 043 after a character has been deleted requires restoring
the referenced player rows from backup first, because wallet and combat history is deliberately
preserved with a nullable player reference.

Older migrations without rollback are not modified because doing so changes their checksum.
Their supported recovery path is database restore plus a forward corrective migration.

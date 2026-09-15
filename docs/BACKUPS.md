# Database backups

Supabase's free tier (what this project runs on) doesn't include any automatic backups, so
[.github/workflows/db-backup.yml](../.github/workflows/db-backup.yml) does it instead.

## What it does

On the 1st of every month (and on-demand via `workflow_dispatch`):

1. `pg_dump`s the production Supabase database (custom format, via a `postgres:16` Docker
   container so the client version matches the server).
2. Encrypts the dump with GPG (AES256, symmetric) using the `DB_BACKUP_PASSPHRASE` secret.
3. Deletes the previous `db-backup` GitHub Release and publishes the new encrypted file as the
   sole asset of a fresh one with that same tag.

Only **one** backup is ever kept — each run replaces the last. This repo is public, so the dump
is always encrypted before it's stored; the release itself is visible, but its asset content
isn't readable without the passphrase.

## Required secrets

Set these in Settings → Secrets and variables → Actions:

| Secret | Notes |
|---|---|
| `POSTGRES_HOST` | Same Supabase Session pooler host used for the Northflank deployment (see [README.md](../README.md#one-time-northflank-setup)) |
| `POSTGRES_PORT` | Usually `5432` |
| `POSTGRES_DB` | Usually `postgres` |
| `POSTGRES_USER` | Supabase pooler username |
| `POSTGRES_PASSWORD` | Supabase pooler password |
| `DB_BACKUP_PASSPHRASE` | A strong random passphrase (e.g. `openssl rand -base64 32`), used only to encrypt/decrypt backups. **Store it somewhere durable outside GitHub too** — if it's lost, existing backups become unrecoverable. |

## Restoring a backup

```bash
gh release download db-backup -p '*.gpg' -O db-backup.dump.gpg

gpg --decrypt --batch --passphrase "$DB_BACKUP_PASSPHRASE" \
  -o db-backup.dump db-backup.dump.gpg

pg_restore --no-owner --no-privileges -d "$TARGET_DATABASE_URL" db-backup.dump
```

Restore against a scratch/staging database first to verify the dump, never directly against
production.

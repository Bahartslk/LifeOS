-- CreateTable
CREATE TABLE "users" (
    "id" UUID NOT NULL,
    "email" TEXT NOT NULL,
    "password_hash" TEXT NOT NULL,
    "display_name" TEXT NOT NULL,
    "avatar_url" TEXT,
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMPTZ NOT NULL,
    "deleted_at" TIMESTAMPTZ,

    CONSTRAINT "users_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
-- Case-insensitive uniqueness (docs/14-database-design.md#schema): a plain
-- unique index on "email" would only reject exact-case duplicates.
-- Prisma's schema DSL cannot express a functional/expression index, so this
-- is hand-written rather than generated from schema.prisma.
CREATE UNIQUE INDEX "users_email_lower_key" ON "users" (lower("email"));

-- CreateIndex
-- Partial index for fast active-account lookups independent of retained
-- soft-deleted rows (docs/14-database-design.md#indexing-strategy).
CREATE INDEX "users_active_id_idx" ON "users" ("id") WHERE "deleted_at" IS NULL;

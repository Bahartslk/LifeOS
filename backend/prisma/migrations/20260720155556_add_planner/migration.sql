-- CreateEnum
CREATE TYPE "TaskPriority" AS ENUM ('LOW', 'MEDIUM', 'HIGH');

-- CreateEnum
CREATE TYPE "TaskStatus" AS ENUM ('TODO', 'IN_PROGRESS', 'DONE');

-- CreateEnum
CREATE TYPE "TaskSource" AS ENUM ('PLANNER', 'HOME', 'TRAVEL', 'AI_ASSISTANT');

-- CreateEnum
CREATE TYPE "TaskListType" AS ENUM ('PERSONAL', 'WORK', 'TRAVEL', 'CUSTOM');

-- CreateTable
CREATE TABLE "task_lists" (
    "id" UUID NOT NULL,
    "user_id" UUID NOT NULL,
    "name" TEXT NOT NULL,
    "type" "TaskListType" NOT NULL DEFAULT 'CUSTOM',
    "color_tag" TEXT,
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted_at" TIMESTAMPTZ,

    CONSTRAINT "task_lists_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "tasks" (
    "id" UUID NOT NULL,
    "user_id" UUID NOT NULL,
    "task_list_id" UUID,
    "title" TEXT NOT NULL,
    "description" TEXT,
    "due_date" DATE NOT NULL,
    "due_time" TIME,
    "priority" "TaskPriority" NOT NULL,
    "status" "TaskStatus" NOT NULL DEFAULT 'TODO',
    "source" "TaskSource" NOT NULL DEFAULT 'PLANNER',
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMPTZ NOT NULL,
    "deleted_at" TIMESTAMPTZ,

    CONSTRAINT "tasks_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
-- Partial (docs/14-database-design.md#indexing-strategy): keeps active-list
-- lookups fast independent of retained soft-deleted lists. Replaces the
-- plain btree Prisma would otherwise generate for `@@index([userId])`.
CREATE INDEX "task_lists_user_id_idx" ON "task_lists"("user_id") WHERE "deleted_at" IS NULL;

-- CreateIndex
CREATE INDEX "tasks_task_list_id_idx" ON "tasks"("task_list_id");

-- CreateIndex
-- Partial composite (docs/14-database-design.md#indexing-strategy):
-- supports Task List/Calendar/Dashboard "today's tasks" queries
-- (`WHERE user_id = ? AND due_date = ?`) without scanning soft-deleted rows.
CREATE INDEX "tasks_user_id_due_date_idx" ON "tasks"("user_id", "due_date") WHERE "deleted_at" IS NULL;

-- AddForeignKey
ALTER TABLE "task_lists" ADD CONSTRAINT "task_lists_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "tasks" ADD CONSTRAINT "tasks_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "tasks" ADD CONSTRAINT "tasks_task_list_id_fkey" FOREIGN KEY ("task_list_id") REFERENCES "task_lists"("id") ON DELETE SET NULL ON UPDATE CASCADE;

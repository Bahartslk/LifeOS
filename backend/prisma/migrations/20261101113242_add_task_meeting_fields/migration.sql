-- CreateEnum
CREATE TYPE "TaskType" AS ENUM ('TASK', 'MEETING');

-- AlterTable
ALTER TABLE "tasks" ADD COLUMN     "duration_minutes" INTEGER,
ADD COLUMN     "location" TEXT,
ADD COLUMN     "reminder_minutes_before" INTEGER,
ADD COLUMN     "type" "TaskType" NOT NULL DEFAULT 'TASK';

-- CreateEnum
CREATE TYPE "TaskCategory" AS ENUM ('WORK', 'PERSONAL', 'HEALTH', 'TRAVEL', 'FINANCE');

-- AlterTable
ALTER TABLE "tasks" ADD COLUMN     "category" "TaskCategory" NOT NULL DEFAULT 'PERSONAL';

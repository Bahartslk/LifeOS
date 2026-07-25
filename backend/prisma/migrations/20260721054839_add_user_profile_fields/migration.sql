-- CreateEnum
CREATE TYPE "ThemeMode" AS ENUM ('SYSTEM', 'LIGHT', 'DARK');

-- AlterTable
ALTER TABLE "users" ADD COLUMN     "bio" TEXT,
ADD COLUMN     "language" TEXT NOT NULL DEFAULT 'tr',
ADD COLUMN     "notification_preferences" JSONB NOT NULL DEFAULT '{"taskReminders": true, "tripReminders": true, "aiInsights": true}',
ADD COLUMN     "theme_preference" "ThemeMode" NOT NULL DEFAULT 'SYSTEM',
ADD COLUMN     "timezone" TEXT NOT NULL DEFAULT 'Europe/Istanbul';

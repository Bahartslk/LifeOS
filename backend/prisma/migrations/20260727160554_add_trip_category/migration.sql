-- CreateEnum
CREATE TYPE "TripCategory" AS ENUM ('RELAX', 'ADVENTURE', 'LUXURY', 'FAMILY', 'BUSINESS');

-- AlterTable
ALTER TABLE "trips" ADD COLUMN     "category" "TripCategory" NOT NULL DEFAULT 'RELAX';

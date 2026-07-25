-- CreateEnum
CREATE TYPE "TripStatus" AS ENUM ('PLANNED', 'ONGOING', 'COMPLETED', 'CANCELLED');

-- CreateEnum
CREATE TYPE "ItineraryItemType" AS ENUM ('FLIGHT', 'ACCOMMODATION', 'ACTIVITY', 'TRANSPORTATION', 'MEAL', 'OTHER');

-- CreateEnum
CREATE TYPE "TransportationType" AS ENUM ('FLIGHT', 'TRAIN', 'BUS', 'CAR', 'FERRY', 'OTHER');

-- CreateEnum
CREATE TYPE "AccommodationType" AS ENUM ('HOTEL', 'HOSTEL', 'APARTMENT', 'GUESTHOUSE', 'OTHER');

-- CreateTable
CREATE TABLE "trips" (
    "id" UUID NOT NULL,
    "user_id" UUID NOT NULL,
    "title" TEXT NOT NULL,
    "description" TEXT,
    "destination" TEXT NOT NULL,
    "country" TEXT NOT NULL,
    "start_date" DATE NOT NULL,
    "end_date" DATE NOT NULL,
    "status" "TripStatus" NOT NULL DEFAULT 'PLANNED',
    "cover_image_url" TEXT,
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMPTZ NOT NULL,
    "deleted_at" TIMESTAMPTZ,

    CONSTRAINT "trips_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "itinerary_items" (
    "id" UUID NOT NULL,
    "trip_id" UUID NOT NULL,
    "title" TEXT NOT NULL,
    "description" TEXT,
    "date" DATE NOT NULL,
    "start_time" TIME,
    "end_time" TIME,
    "location" TEXT,
    "order_index" INTEGER NOT NULL,
    "type" "ItineraryItemType" NOT NULL,
    "transportation_type" "TransportationType",
    "accommodation_type" "AccommodationType",
    "task_id" UUID,
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMPTZ NOT NULL,

    CONSTRAINT "itinerary_items_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "itinerary_items_task_id_key" ON "itinerary_items"("task_id");

-- CreateIndex
CREATE INDEX "itinerary_items_trip_id_idx" ON "itinerary_items"("trip_id");

-- CreateIndex
-- Partial composite (docs/14-database-design.md#indexing-strategy):
-- supports "My Trips" listing grouped/sorted by date without scanning
-- soft-deleted rows. No matching `@@index` in schema.prisma — see `Trip`'s
-- doc comment for why (avoids the drift-detection loop `TaskList` already
-- documents).
CREATE INDEX "trips_user_id_start_date_idx" ON "trips"("user_id", "start_date") WHERE "deleted_at" IS NULL;

-- AddForeignKey
ALTER TABLE "trips" ADD CONSTRAINT "trips_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "itinerary_items" ADD CONSTRAINT "itinerary_items_trip_id_fkey" FOREIGN KEY ("trip_id") REFERENCES "trips"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "itinerary_items" ADD CONSTRAINT "itinerary_items_task_id_fkey" FOREIGN KEY ("task_id") REFERENCES "tasks"("id") ON DELETE SET NULL ON UPDATE CASCADE;

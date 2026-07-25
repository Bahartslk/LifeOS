import { Injectable, Logger, OnModuleDestroy, OnModuleInit } from '@nestjs/common';
import { PrismaClient } from '@prisma/client';

/**
 * Thin wrapper around PrismaClient managing connection lifecycle. Every
 * feature repository will inject this service rather than instantiating its
 * own PrismaClient, per docs/12-project-architecture.md#repository-pattern-backend
 * ("Repositories abstract PostgreSQL access behind interfaces").
 *
 * This bootstrap intentionally defines no models on the Prisma schema (see
 * prisma/schema.prisma) — this service only manages the connection.
 */
@Injectable()
export class PrismaService extends PrismaClient implements OnModuleInit, OnModuleDestroy {
  private readonly logger = new Logger(PrismaService.name);

  async onModuleInit(): Promise<void> {
    await this.$connect();
    this.logger.log('Prisma connected to PostgreSQL');
  }

  async onModuleDestroy(): Promise<void> {
    await this.$disconnect();
  }
}

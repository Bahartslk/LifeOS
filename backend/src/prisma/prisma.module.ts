import { Global, Module } from '@nestjs/common';
import { PrismaService } from './prisma.service';

/**
 * Global module so every feature module can inject PrismaService without
 * re-importing this module everywhere, matching how a connection pool is a
 * single, shared resource per docs/12-project-architecture.md#scalability--production-readiness.
 */
@Global()
@Module({
  providers: [PrismaService],
  exports: [PrismaService],
})
export class PrismaModule {}

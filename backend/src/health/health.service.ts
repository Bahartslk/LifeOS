import { readFileSync } from 'fs';
import { join } from 'path';
import { Injectable } from '@nestjs/common';
import { HealthResponseDto } from './dto/health-response.dto';

/**
 * Kept as a service (rather than inlined in the controller) purely to
 * respect this project's "controllers, services, repositories — no layer
 * skipping" rule (CLAUDE.md#backend-guidelines), even though the check
 * itself has no business logic yet. A real dependency check (e.g.
 * `PrismaService`-backed `SELECT 1`) is a natural extension point here once
 * a sprint needs the health check to reflect actual database reachability,
 * not just process liveness.
 *
 * `package.json`'s version is read via `fs.readFileSync` at runtime,
 * relative to `__dirname`, rather than a static `import … from
 * '../../package.json'` — a static import pulls a file from outside `src/`
 * into the compiler's module graph, which silently widens `tsc`'s inferred
 * output root and nests every compiled file one directory deeper (breaking
 * the Dockerfile's `dist/main.js` entrypoint). `../../package.json` resolves
 * correctly from both `src/health` (dev) and `dist/health` (build output),
 * since both sit at the same depth under the project root.
 */
@Injectable()
export class HealthService {
  private readonly version: string = JSON.parse(
    readFileSync(join(__dirname, '../../package.json'), 'utf8'),
  ).version;

  check(): HealthResponseDto {
    return {
      status: 'ok',
      timestamp: new Date().toISOString(),
      version: this.version,
    };
  }
}

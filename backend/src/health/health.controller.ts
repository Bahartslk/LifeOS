import { Controller, Get, VERSION_NEUTRAL } from '@nestjs/common';
import { ApiOkResponse, ApiOperation, ApiTags } from '@nestjs/swagger';
import { SkipResponseTransform } from '../common/decorators/skip-response-transform.decorator';
import { HealthResponseDto } from './dto/health-response.dto';
import { HealthService } from './health.service';

/**
 * Unversioned, prefix-free `GET /health` — infra tooling (Docker
 * healthcheck, load balancers, k8s probes) needs one stable path regardless
 * of API version. Getting a bare `/health` needs both `main.ts`'s
 * `setGlobalPrefix` exclude list (strips `/api`) *and* `version:
 * VERSION_NEUTRAL` here (opts out of the `/v1` URI-versioning segment,
 * which `setGlobalPrefix`'s exclude does not affect — the two are
 * independent mechanisms).
 */
@ApiTags('health')
@Controller({ path: 'health', version: VERSION_NEUTRAL })
export class HealthController {
  constructor(private readonly healthService: HealthService) {}

  @Get()
  @SkipResponseTransform()
  @ApiOperation({ summary: 'Liveness check' })
  @ApiOkResponse({ type: HealthResponseDto })
  check(): HealthResponseDto {
    return this.healthService.check();
  }
}

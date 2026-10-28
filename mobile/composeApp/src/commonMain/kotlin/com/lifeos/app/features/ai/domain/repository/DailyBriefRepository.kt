package com.lifeos.app.features.ai.domain.repository

import com.lifeos.app.features.ai.domain.model.DailyBrief

interface DailyBriefRepository {
    /**
     * Today's brief. A complete brief is kept for the rest of the day so
     * reopening the screen doesn't trigger new AI calls; [forceRefresh]
     * skips that and asks the backend again.
     */
    suspend fun getDailyBrief(forceRefresh: Boolean = false): Result<DailyBrief>
}

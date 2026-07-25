import { Module } from '@nestjs/common';

/**
 * Reserved, empty. No entity or requirement for this module exists yet in
 * docs/14-database-design.md or docs/07-functional-requirements.md — it is
 * registered now purely as an empty shell per this sprint's explicit module
 * list. Its data model, whether it needs its own table versus reusing
 * `user_preferences.notifications_enabled`, and its implementation are all
 * scoped to a future sprint, once product requirements for notifications
 * exist.
 */
@Module({})
export class NotificationsModule {}

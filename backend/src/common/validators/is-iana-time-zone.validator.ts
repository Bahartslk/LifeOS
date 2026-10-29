import { ValidationOptions, registerDecorator } from 'class-validator';
import { isIanaTimeZone } from '../utils/date-time.util';

/** class-validator decorator: the property must be an IANA time zone name (see `isIanaTimeZone`). */
export function IsIanaTimeZone(validationOptions?: ValidationOptions): PropertyDecorator {
  return (target: object, propertyName: string | symbol) => {
    registerDecorator({
      name: 'isIanaTimeZone',
      target: target.constructor,
      propertyName: propertyName.toString(),
      options: {
        message: '$property must be an IANA time zone name (e.g. "Europe/Istanbul" or "UTC").',
        ...validationOptions,
      },
      validator: {
        validate: (value: unknown) => isIanaTimeZone(value),
      },
    });
  };
}

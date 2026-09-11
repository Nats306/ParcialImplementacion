export type RaceType = 'INDIVIDUAL' | 'TEAM' | 'MIXED';
export type RaceStatus =
  | 'DRAFT'
  | 'OPEN_FOR_REGISTRATION'
  | 'CLOSED_FOR_REGISTRATION'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export interface Race {
  id: number;
  name: string;
  description: string;
  scheduledDateTime: string;
  startLocation: string;
  finishLocation: string;
  distanceMeters: number;
  maxParticipants: number;
  raceType: RaceType;
  raceStatus: RaceStatus;
  organizer: string;
  registrationDeadline: string;
  creationDate: string;
  lastModificationDate: string;
}

export interface RaceRequest {
  name: string;
  description: string;
  scheduledDateTime: string;
  startLocation: string;
  finishLocation: string;
  distanceMeters: number;
  maxParticipants: number;
  raceType: RaceType;
  organizer: string;
  registrationDeadline: string;
}

export const RACE_TYPES: RaceType[] = ['INDIVIDUAL', 'TEAM', 'MIXED'];
export const RACE_STATUSES: RaceStatus[] = [
  'DRAFT',
  'OPEN_FOR_REGISTRATION',
  'CLOSED_FOR_REGISTRATION',
  'IN_PROGRESS',
  'COMPLETED',
  'CANCELLED',
];

/**
 * Transiciones de estado permitidas desde cada estado. Se usa para no
 * mostrarle al usuario botones que el backend va a rechazar de todas
 * formas (la validación real sigue siendo del backend, esto es solo UX).
 */
export const RACE_STATUS_TRANSITIONS: Record<RaceStatus, RaceStatus[]> = {
  DRAFT: ['OPEN_FOR_REGISTRATION', 'CANCELLED'],
  OPEN_FOR_REGISTRATION: ['CLOSED_FOR_REGISTRATION', 'CANCELLED'],
  CLOSED_FOR_REGISTRATION: ['IN_PROGRESS', 'CANCELLED'],
  IN_PROGRESS: ['COMPLETED', 'CANCELLED'],
  COMPLETED: [],
  CANCELLED: [],
};

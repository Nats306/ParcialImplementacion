export type RegistrationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export interface RaceRegistration {
  id: number;
  raceId: number;
  raceName: string;
  competitorId: string | null;
  competitorName: string | null;
  teamId: number | null;
  teamName: string | null;
  registrationDate: string;
  status: RegistrationStatus;
  startingPosition: number;
  validationNotes: string | null;
  registeredBy: string;
}

export interface RaceRegistrationRequest {
  competitorId?: string;
  teamId?: number;
  startingPosition: number;
  registeredBy: string;
}

export interface RejectRegistrationRequest {
  reason: string;
}

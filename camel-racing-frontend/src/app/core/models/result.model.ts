export type ResultStatus = 'FINISHED' | 'DISQUALIFIED' | 'DID_NOT_FINISH' | 'DID_NOT_START';

export interface RaceResult {
  id: number;
  raceId: number;
  raceName: string;
  registrationId: number;
  participantType: string;
  competitorId: string | null;
  competitorName: string | null;
  teamId: number | null;
  teamName: string | null;
  startingPosition: number;
  finalPosition: number | null;
  completionTimeMillis: number | null;
  penaltyTimeMillis: number;
  totalTimeMillis: number | null;
  resultStatus: ResultStatus;
  points: number;
  notes: string | null;
  recordedBy: string;
  recordedAt: string;
}

export interface RaceResultRequest {
  registrationId: number;
  resultStatus: ResultStatus;
  finalPosition?: number;
  completionTimeMillis?: number;
  penaltyTimeMillis: number;
  notes?: string;
  recordedBy: string;
}

export const RESULT_STATUSES: ResultStatus[] = [
  'FINISHED',
  'DISQUALIFIED',
  'DID_NOT_FINISH',
  'DID_NOT_START',
];

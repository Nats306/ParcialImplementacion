export type CompetitorType = 'DWARF' | 'CAMEL' | 'MEDIUM' | 'OTHER';
export type CompetitorStatus = 'ACTIVE' | 'INJURED' | 'SUSPENDED' | 'RETIRED';

export interface Competitor {
  id: string;
  name: string;
  nickname: string;
  competitorType: CompetitorType;
  age: number;
  weight: number;
  height: number;
  country: string;
  currentStatus: CompetitorStatus;
  registrationDate: string;
  victories: number;
  defeats: number;
  completedRaces: number;
}

export interface CompetitorRequest {
  name: string;
  nickname: string;
  competitorType: CompetitorType;
  age: number;
  weight: number;
  height: number;
  country: string;
}

export const COMPETITOR_TYPES: CompetitorType[] = ['DWARF', 'CAMEL', 'MEDIUM', 'OTHER'];
export const COMPETITOR_STATUSES: CompetitorStatus[] = ['ACTIVE', 'INJURED', 'SUSPENDED', 'RETIRED'];

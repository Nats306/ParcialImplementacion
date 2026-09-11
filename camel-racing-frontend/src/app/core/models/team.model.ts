export type TeamStatus = 'ACTIVE' | 'SUSPENDED' | 'INACTIVE';

export interface Member {
  id: string;
  name: string;
  nickname: string;
}

export interface Team {
  id: number;
  name: string;
  description: string;
  creationDate: string;
  coach: string;
  status: TeamStatus;
  victories: number;
  defeats: number;
  memberCount: number;
  members: Member[];
}

export interface TeamRequest {
  name: string;
  description: string;
  coach: string;
}

export const TEAM_STATUSES: TeamStatus[] = ['ACTIVE', 'SUSPENDED', 'INACTIVE'];

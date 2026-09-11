export interface CompetitorStanding {
  rank: number;
  competitorId: string;
  name: string;
  nickname: string;
  points: number;
  victories: number;
  secondPlaces: number;
  thirdPlaces: number;
  racesWithResults: number;
}

export interface TeamStanding {
  rank: number;
  teamId: number;
  name: string;
  points: number;
  victories: number;
  secondPlaces: number;
  thirdPlaces: number;
  racesWithResults: number;
}

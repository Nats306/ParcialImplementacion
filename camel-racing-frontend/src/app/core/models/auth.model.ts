/** Los tres roles del realm de Keycloak (ver keycloak/realm-export.json del backend). */
export type Role = 'ADMINISTRATOR' | 'RACE_ORGANIZER' | 'VIEWER';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  refresh_expires_in: number;
  token_type: string;
}

export interface UserProfile {
  subject: string;
  username: string;
  email: string;
  fullName: string;
  roles: Role[];
}

export type AuditAction =
  | 'LOGIN'
  | 'CREATE'
  | 'UPDATE'
  | 'DELETE'
  | 'STATUS_CHANGE'
  | 'CANCEL'
  | 'APPROVE'
  | 'REJECT'
  | 'REGISTER'
  | 'ADD_MEMBER'
  | 'REMOVE_MEMBER';

export interface AuditLog {
  id: number;
  username: string;
  action: AuditAction;
  entityType: string;
  entityId: string | null;
  timestamp: string;
  description: string | null;
  previousValue: string | null;
  newValue: string | null;
}

export const AUDIT_ACTIONS: AuditAction[] = [
  'LOGIN',
  'CREATE',
  'UPDATE',
  'DELETE',
  'STATUS_CHANGE',
  'CANCEL',
  'APPROVE',
  'REJECT',
  'REGISTER',
  'ADD_MEMBER',
  'REMOVE_MEMBER',
];

export const AUDIT_ENTITY_TYPES: string[] = ['Auth', 'Competitor', 'Team', 'Race', 'Registration', 'Result'];

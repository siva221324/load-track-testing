export type UserRole = 'ADMIN' | 'DRIVER' | 'DEALER';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  role: UserRole;
  userId: number;
}

export interface DecodedToken {
  sub: string;       // username
  role: UserRole;
  userId: number;
  iat: number;
  exp: number;
}

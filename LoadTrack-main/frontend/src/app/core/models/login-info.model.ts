export interface CreateLoginRequest {
  username: string;
  password: string;
}

export interface LoginInfo {
  userId: number;
  username: string;
  role: 'ADMIN' | 'DRIVER' | 'DEALER';
  linkedToId: number;
  linkedToName: string;
}

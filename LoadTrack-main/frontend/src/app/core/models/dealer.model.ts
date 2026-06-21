import { LoginInfo } from './login-info.model';

export interface Dealer {
  id: number;
  name: string;
  phone: string;
  address?: string;
  loginInfo?: LoginInfo | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface DealerRequest {
  name: string;
  phone: string;
  address?: string;
}

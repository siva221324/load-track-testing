export interface SandType {
  id: number;
  name: string;
  pricePerTon: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface SandTypeRequest {
  name: string;
  pricePerTon: number;
}

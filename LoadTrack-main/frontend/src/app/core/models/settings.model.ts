export interface Settings {
  id: number;
  interestRatePercent: number;
  allowedDays: number;
  updatedAt?: string;
}

export interface SettingsRequest {
  interestRatePercent: number;
  allowedDays: number;
}

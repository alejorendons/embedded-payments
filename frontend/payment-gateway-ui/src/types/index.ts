// API Response Types
export interface ApiResponse<T> {
  data?: T;
  error?: string;
  message?: string;
  status: number;
}

export interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}

// Auth Types
export interface User {
  id: string;
  email: string;
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'DISABLED';
  role?: 'ROLE_ADMIN' | 'ROLE_MERCHANT' | 'ROLE_USER';
  merchantId?: string;
  createdAt?: Date;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  expiresAt: string;
}

// Merchant Types
export interface Merchant {
  id: string;
  name: string;
  email: string;
  contact_name?: string;
  contact_email?: string;
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'DISABLED';
  bank_account_data?: string;
  updated_at?: Date;
  created_at?: Date;
  updatedAt?: Date;
  createdAt?: Date;
}

export interface UpdateContactRequest {
  contact_name: string;
  contact_email: string;
}

export interface RegisterBankAccountRequest {
  iban: string;
  routing_number: string;
  account_holder_name: string;
}

export interface ChangeMerchantStatusRequest {
  reason: string;
}

// Transaction Types
export interface Transaction {
  id: string;
  merchantId: string;
  amount: number;
  currency: string;
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';
  customerEmail: string;
  customerName?: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface Refund {
  id: string;
  transactionId: string;
  amount: number;
  reason: string;
  status: 'PENDING' | 'COMPLETED' | 'FAILED';
  createdAt: Date;
}

// Report Types
export interface SalesReport {
  totalRevenue: number;
  totalTransactions: number;
  averageOrderValue: number;
  period: string;
}

export interface TransactionReport {
  transactions: Transaction[];
  total: number;
  period: string;
}

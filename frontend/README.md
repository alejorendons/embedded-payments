# Payment Gateway UI - Frontend

Vue 3 + TypeScript + Vite frontend application for the Embedded Payments Platform.

## Quick Start

### Prerequisites
- Node.js 18+
- Backend running on `http://localhost:8085`

### Installation & Development

```bash
cd frontend/payment-gateway-ui

# Install dependencies
npm install

# Start development server
npm run dev

# App will be available at http://localhost:5173
```

### Environment Variables

Create `.env.development` in `frontend/payment-gateway-ui/`:

```env
VITE_API_URL=http://localhost:8085
VITE_APP_NAME="Payment Gateway (Development)"
VITE_APP_VERSION=0.1.0
VITE_LOG_LEVEL=debug
```

## Features

### Authentication
- Login with email/password
- JWT token management
- Protected routes with auth guards
- Logout functionality

### Merchant Management
- View merchant status (ACTIVE/INACTIVE)
- Activate/Deactivate account with reason
- Update contact information
- Register bank account details (IBAN, Routing Number)
- Encryption of sensitive data

### Transactions
- View paginated transaction list
- View transaction details
- Process refunds for completed transactions
- Transaction status tracking (PENDING, COMPLETED, FAILED, REFUNDED)

### Dashboard
- At-a-glance merchant overview
- Quick access to key operations
- Contact and bank account info display
- Account status management

## Project Structure

```
src/
├── pages/           # Vue page components
│   ├── login.vue
│   ├── dashboard.vue
│   ├── settings/    # Contact, Bank, Profile settings
│   └── transactions/
├── components/      # Reusable components
├── layouts/         # Layout wrappers
├── services/        # API client services
├── stores/          # Pinia state management
├── types/           # TypeScript interfaces
└── App.vue         # Root component
```

## Available Scripts

```bash
npm run dev          # Start dev server
npm run build        # Build for production
npm run preview      # Preview production build
npm run lint         # Lint code
npm run type-check   # TypeScript type checking
npm run test         # Run tests
npm run test:ui      # Run tests with UI
npm run test:e2e     # Run end-to-end tests
```

## API Integration

The frontend communicates with the backend via REST API:

- Base URL: `http://localhost:8085/api/v1`
- Authentication: JWT Bearer token in `Authorization` header
- All requests/responses use JSON format

### Key Endpoints

- `POST /auth/login` - User authentication
- `GET /merchants/{id}` - Get merchant details
- `PUT /merchants/{id}/contact` - Update contact info
- `PUT /merchants/{id}/bank-account` - Register bank account
- `PATCH /merchants/{id}/activate` - Activate merchant
- `PATCH /merchants/{id}/deactivate` - Deactivate merchant
- `GET /transactions` - List transactions
- `GET /transactions/{id}` - Get transaction details
- `POST /refunds` - Process refund

## State Management

Uses Pinia for state management:

- `authStore` - Authentication state
- `merchantStore` - Merchant data and operations
- `notificationStore` - Global notifications
- `transactionStore` - Transaction data (can be extended)

## Styling

Tailwind CSS for utility-first styling with custom theme colors:
- Primary: Indigo (600/700)
- Success: Green
- Error: Red
- Warning: Yellow
- Info: Blue

## Browser Support

- Modern browsers (Chrome, Firefox, Safari, Edge)
- ES2020+ JavaScript support required

## Troubleshooting

### Dev server not starting
- Ensure all dependencies are installed: `npm install`
- Check Node.js version (18+)
- Clear node_modules and reinstall if needed

### API connection errors
- Verify backend is running on port 8085
- Check CORS configuration in backend
- Verify correct API URL in .env file

### Authentication issues
- Clear localStorage (tokens)
- Verify backend login endpoint works
- Check token format and expiration

## Development Guidelines

- Use TypeScript for type safety
- Follow Vue 3 Composition API patterns
- Keep components small and focused
- Use stores for shared state
- Add proper error handling
- Include loading states for async operations

## Contributing

Follow these practices:
- One feature/fix per commit
- Clear commit messages
- Test changes before committing
- Keep code clean with linting

## License

MIT

# Frontend — Trulia Clone

React + Vite single-page application for the Trulia Clone platform.

## Tech Stack

- **React 19** with React Router DOM v7
- **Vite 6** for development and bundling
- **Tailwind CSS** for styling
- **Redux Toolkit** for global state management
- **Axios** for API calls (all routed through `http://localhost:8080`)
- **Stripe.js** (`@stripe/react-stripe-js`) for payment UI
- **Leaflet** + **Google Maps** (`@vis.gl/react-google-maps`) for property maps
- **Recharts** for revenue/analytics charts
- **React Toastify** for notifications

## Structure

```
frontend/
├── public/
├── index.html
├── src/
│   ├── main.jsx                  # App entry point
│   ├── App.jsx                   # Route definitions
│   │
│   ├── components/               # Reusable UI components
│   │   ├── Header.jsx
│   │   ├── Footer.jsx
│   │   ├── Hero.jsx
│   │   ├── FilterBar.jsx         # Price/size/type filters with debounced sliders
│   │   ├── PropertyCard.jsx
│   │   ├── PropertyList.jsx
│   │   ├── PropertyDetails.jsx   # Detail view + rental payment flow
│   │   ├── SignIn.jsx            # Login form + OAuth2 buttons (Google, GitHub)
│   │   ├── SignUp.jsx
│   │   ├── UserProfile.jsx
│   │   ├── ProtectedRoute.jsx
│   │   ├── VerifyEmail.jsx
│   │   ├── ForgotPassword.jsx
│   │   ├── ResetPassword.jsx
│   │   ├── ScheduleTour.jsx
│   │   ├── RequestInfo.jsx
│   │   ├── SavedHomesButton.jsx
│   │   ├── seller/
│   │   │   ├── SellerDashboard.jsx
│   │   │   ├── PropertyForm.jsx
│   │   │   ├── PropertyListingTable.jsx
│   │   │   ├── PropertyMapForm.jsx
│   │   │   ├── TransactionList.jsx
│   │   │   └── ScheduledToursList.jsx
│   │   └── admin/
│   │       ├── AdminDashboard.jsx
│   │       ├── UserManagement.jsx
│   │       ├── ArticleManagement.jsx
│   │       └── RevenueManagement.jsx
│   │
│   ├── pages/                    # Full-page route components
│   │   ├── SavedHomes.jsx
│   │   ├── SavedSearches.jsx
│   │   ├── PaymentSuccess.jsx
│   │   ├── PaymentCancel.jsx
│   │   └── OAuth2Callback.jsx    # Handles redirect after OAuth2 login
│   │
│   ├── redux/
│   │   ├── store.js
│   │   └── slices/
│   │       ├── authSlice.js      # Login, logout, register, loginWithToken (OAuth2)
│   │       ├── propertySlice.js
│   │       ├── userSlice.js
│   │       ├── savedHomesSlice.js
│   │       ├── savedSearchesSlice.js
│   │       └── messageSlice.js
│   │
│   ├── services/                 # API call functions
│   │   ├── authService.js        # Auth endpoints + OAuth2 token storage
│   │   ├── authHeader.js         # Injects Bearer token into request headers
│   │   ├── propertyService.js
│   │   ├── userService.js
│   │   ├── transactionService.js
│   │   ├── rentalService.js
│   │   ├── paymentService.js     # Stripe checkout session creation
│   │   └── adminService.js
│   │
│   └── config/
│       └── stripe.js             # Stripe publishable key
│
├── nginx.conf                    # Nginx config for Docker production build
├── Dockerfile                    # Multi-stage: Node 20 build → Nginx serve
├── tailwind.config.js
├── vite.config.js
└── package.json
```

## Routes

| Path | Component | Auth |
|---|---|---|
| `/` | Home (Hero + FilterBar + PropertyList) | Public |
| `/property/:id` | PropertyDetails | Public |
| `/signin` | SignIn | Public |
| `/signup` | SignUp | Public |
| `/verify` | VerifyEmail | Public |
| `/forgot-password` | ForgotPassword | Public |
| `/reset-password/:token` | ResetPassword | Public |
| `/oauth2/callback` | OAuth2Callback | Public |
| `/payment/success` | PaymentSuccess | Public |
| `/payment/cancel` | PaymentCancel | Public |
| `/profile` | UserProfile | Protected |
| `/dashboard` | SellerDashboard | Protected |
| `/admin` | AdminDashboard | Protected |
| `/add-property` | PropertyForm | Protected |
| `/edit-property/:id` | PropertyForm | Protected |
| `/saved-homes` | SavedHomes | Protected |
| `/saved-searches` | SavedSearches | Protected |

## Running Locally

```bash
cd frontend
npm install
npm run dev
```

The dev server runs on `http://localhost:5173`. Make sure the backend API Gateway is running on `http://localhost:8080`.

## Docker

```bash
cd frontend
docker build -t trulia-frontend .
docker run -p 3000:80 trulia-frontend
```

The Dockerfile does a production build with Node 20 and serves it via Nginx on port 80.

## Authentication

- Auth state is stored in Redux and persisted to `localStorage` (token + user object).
- All Axios instances attach `Authorization: Bearer <token>` via a request interceptor in `authService.js`.
- OAuth2 sign-in (Google / GitHub) redirects through the backend and lands on `/oauth2/callback?token=xxx`, where the token is parsed from the JWT and saved.
- Role-based redirects after login: `ROLE_ADMIN` → `/admin`, `ROLE_SELLER` → `/dashboard`, others → `/`.

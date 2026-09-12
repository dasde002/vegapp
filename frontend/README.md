# FreshBasket frontend

Phase 3 frontend for the Vegetable Marketplace backend.

## Included
- Customer registration and JWT login
- Product catalog with search and category filter
- Cart quantity updates and removal
- Address management
- Checkout and order tracking
- Seller dashboard
- Seller inventory updates
- Seller order status management
- Admin user/product view and product activation controls
- Responsive desktop/mobile UI

## Run

```bash
cd frontend
npm install
npm run dev
```

The Vite development server listens on `0.0.0.0:5173` and proxies `/api` to the Spring Boot backend on `127.0.0.1:8080`.

Build for production:

```bash
npm run build
```

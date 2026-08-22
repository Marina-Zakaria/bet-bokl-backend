Home Rental Service  —  Local backend for mobile dev
=====================================================

1. Load the updated image:

     docker load -i home-rental-service-renter-reviews-20260823.tar

2. Existing installation (preserves the database and uploaded-data references):

     docker compose up -d db
     docker compose up -d --no-deps --force-recreate app

   Liquibase automatically applies migrations 012, 013, and 014 to the existing database.
   Migration 013 adds owner approval, seeded instant-booking terms (ID 1001),
   and booking_approval_expiration_hours (default 24).
   Migration 014 replaces COMPOUND/TOURIST with PREMIUM/HOTEL, stores Arabic
   and English category labels, and seeds the automatic price boundaries.
   This image also adds GET /api/renters/{renterId}/reviews. No new database
   migration is required for the renter-reviews endpoint.
   Do NOT run "docker compose down -v"; "-v" deletes the database volume.

3. Fresh installation:

     docker compose up -d

   The API is available at http://localhost:8080
   Health check:  GET http://localhost:8080/health

4. Stop everything without deleting data:

     docker compose down

5. Only when you intentionally want to erase the database:

     docker compose down -v

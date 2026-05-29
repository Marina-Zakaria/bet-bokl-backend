Home Rental Service  —  Local backend for mobile dev
=====================================================

1. Load the image into Docker (one time per release):

     docker load -i home-rental-service-v1.0.0.tar

2. Start the backend + database:

     docker compose up -d

   The API is available at http://localhost:8080
   Health check:  GET http://localhost:8080/health

3. Stop everything:

     docker compose down

4. To wipe the database and start fresh:

     docker compose down -v

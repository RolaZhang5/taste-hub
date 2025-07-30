# Taste-Hub Application
[![License](https://img.shields.io/badge/license-MIT-green)](./LICENSE)

**Taste-Hub** is a full-stack restaurant review and social platform built with **Spring Boot** and **React**.  
It includes features such as user login, merchant search, nearby merchant recommendations, check-in rewards, friend follow & notifications, and performance optimizations like caching strategies, asynchronous queues, and Feed stream design for high-concurrency scenarios.

This is a Single Page Application (SPA) with front-end and back-end separated into **two branches**. The front-end communicates with the back-end via API calls.

---

## Screenshots
![image](https://github.com/RolaZhang5/taste-hub/blob/main/frontend/public/imgs/homepage.png)
---

## Features
- RESTful API architecture
- User login/registration (SMS verification code & Token login supported)
- Merchant geolocation and "Nearby Merchants" feature (based on Redis GEO)
- Follow/unfollow friends and mutual follow queries
- User Feed stream (timeline + scroll-based pagination)
- Blog like/unlike and like leaderboard (Redis ZSet)
- Daily user check-in reward system (Redis Bitmap)
- Caching optimizations: logical expiration, cache penetration, cache avalanche prevention
- High-concurrency voucher flash sale (Redis Stream asynchronous queue + Lua scripts)

---

## Tech Stack

### Backend
- Spring Boot
- JWT-based user authentication
- MyBatis / MyBatis Plus
- Redis (ZSet, GEO, Bitmap, Stream, Lua scripts)
- MySQL
- Maven

### Frontend
- React
- Ant Design
- Axios
- Vite

## How to Run Locally

> **Start the backend server first, then start the frontend project.**

### Backend

1. Install [MySQL](https://dev.mysql.com/downloads/) and [Redis](https://redis.io/download) (Redis version **6.2+** required).
2. Update the `application.yml` with your database and Redis configurations.
3. Navigate to the backend directory:

    ```
    cd backend
    ```
4. Build the project：
    ```
    mvn install
    ```
5. Run the backend server：

    ```
    mvn spring-boot:run
    ```

6.Backend will be available at: http://localhost:8080

### Frontend
1. Install Node.js and npm.

2. Navigate to the frontend directory:
    ```
    cd frontend
    ```
3. Install dependencies：

    ```
    npm install
    ```

4. Start the development server：
    ```
    npm run dev
    ```
5. Frontend will be available at: http://localhost:3000

**Note**：The API base URL is configured in src/config.js, default: http://localhost:8080/api.

## License
This project is licensed under the MIT license.
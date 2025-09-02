# 📖 NovelReaderApp – Project Documentation For LLM

* Do not be creative unless asked
* Short answers related to the question
* Request for the documentation if needed
* No Room related until further understanding.

## Current State

* Only scraping RoyalRoad (Latests pages, Best Rated pages, Searchbar pages, Genre Filter)
* Basic tts and font size
* Added backend integration

## Vision

* NovelReaderApp is a personal project mobile app for reading web novels using scrapers.
* Inspired by [Bernso/NovelReaderWeb](https://github.com/Bernso/NovelReaderWeb), [NovelReaderWeb](https://bernso.pythonanywhere.com).
* Backend API using Node.js, Express, MongoDB for data persitance [Byron-Backend](https://github.com/ByronDZH/Byron-Backend), With the goal to be something similar to [MyAnimeList].
* Inspiration for EPUB Reader [AquileReader].
* Similar project for comparations [AkashicRecords](https://github.com/Luiz-eduardp/akashic_records).

**Key Goals:**

* Multi-source reading support
* Seamless live-first experience
* EPUB reader for offline experience
* EPUB file creation from scrapers
* Personalization for users (accounts, progress, bookmarks, lists)
* General user configurations (fonts, themes, tts)

---

## 🧭 Screens

* Home Screen: Navbar with app title, profile/login button, and settings icons. Grid of scraper sources, Grid of saved epubs.
* Scrapers Screens: Common functions (Searchbar, Latest Updates, Best Rated), Unique functions of each source (Genre Filter).
* Novel Screen: Novel Title, author, tags, description, list of chapters from the novel.
* Chapter Screen: Navbar with chapter name, settings button, previous/next chapter button. Chapter content.
* Settings Screen: Font size slider, tts button (popback to chapter), themes (planned).
* Auth Screen: Screen for register/login and logged user content.

---

## 🚧 Features – Current, In Progress, Planned

### ✅ Fully Implemented

* Jetpack Compose + Material3 UI
* RoyalRoad integration (novels, chapters, content)
* MVVM architecture with repositories
* Basic live mode
* Font size controls & basic UI navigation
* Chapter navigation cleanup (use IDs, reduce logs)
* TTS
* Search function
* User register, login with JWT, CRUD backend service (Byron-Backend)

### ⚠️ In Progress

* Multi-source integration (placeholders only)
* UI polish & design consistency
* Error handling improvements
* Basic theming

### 💤 Planned

* Favorites & bookmarks
* Reading progress tracking
* Advanced settings (TTS pitch/speed, auto-advance)
* Testing (unit/UI)
* Source scraping for more platforms

---

## Project Structure

```
📁 backendconnection       
├── Repository.kt
├── RetrofitApi.kt
├── RetrofitClient.kt
├── 📁 backendmodels
│   ├── ApiError.kt
│   ├── AuthResponse.kt
│   ├── NovelBackend.kt
│   └── UserCredentials.kt
│   
📁 data
│   
├── 📁 models
│   ├── Novel.kt
│   ├── Chapter.kt
│   └── ChapterJson.kt  # JS parsing support (e.g., window.chapters[] from RoyalRoad)
│
├── 📁 scraper
│   ├── 📁 base                # Interfaces and factories used by scrapers
│   │   ├── NovelScraper.kt    # Optional base interface (can be partial)
│   │   └── ScraperFactory.kt  # Returns correct scraper per source
│   └── RoyalRoadScraper.kt    # 
│   
📁 ui
├── 📁 components         # Reusable UI widgets
│   ├── TopBar.kt, Card.kt, Color.kt, Theme.kt, Type.kt
│   │
│   📁 screens
│   ├── 📁 common          # Screens shared across sources or core features
│   │   ├── AppNavigation.kt # 
│   │   ├── UserAuthScreen.kt
│   │   ├── Routes.kt
│   │   └── SettingsScreen.kt
│   │
│   ├── ChapterContentScreen.kt
│   ├── ChapterListScreen.kt
│   ├── HomeScreen.kt
│   └── RoyalRoadScreen.kt
│
📁 viewmodel
├── 📁 factories
│   └── AuthViewModelFactory.kt
│
├── AuthViewmodel.kt
├── ChapterViewModel.kt
├── RoyalRoadViewModel.kt
└── SettingsViewModel.kt

MainActivity.kt            # Entry point of the app
```

---

## 🧱 How to Extend

1. Create new scraper class: `WebNovelScraper` implementing `NovelScraper`
2. Register in `ScraperFactory`
3. Add to source grid on `HomeScreen.kt`
4. Add navigation + ViewModel support
5. Integrate Room caching
6. Extend UI/UX, user feedback, and testing
7. Create a new screen in `screens/<source>`
8. Use `ScraperFactory.getScraper("sourceId")` to load the correct scraper

---

## ✅ Backend API Features Added to the App (Plans of reconstruction for a service similar to [MyAnimeList])

### 1. User Registration

* **Endpoint:** `POST /users/register`
* **Integration:**

  * `ByronApi.register()` — Retrofit method annotated with `@POST("users/register")`
  * `UserRepository.register()` — wraps the API call, returns `Result<String>` (JWT token or error)
  * `AuthViewModel.register()` — triggers registration via coroutine and updates UI state
  * `AuthScreen` — provides input fields and button to trigger registration

### 2. User Login

* **Endpoint:** `POST /users/login`
* **Integration:**

  * `ByronApi.login()` — Retrofit method annotated with `@POST("users/login")`
  * `UserRepository.login()` — wraps the API call, returns `Result<String>` (JWT token or error)
  * `AuthViewModel.login()` and `loginWithHealthPing()` — call login and update UI state
  * `AuthScreen` — lets user enter credentials and log in

### 3. Health Check (Ping)

* **Endpoint:** `GET /system/ping`
* **Response Example:**

  ```json
  {
    "message": "Pong! Service is running.",
    "timestamp": "2025-08-05T15:35:38.678Z"
  }
  ```
* **Integration:**

  * `ByronApi.ping()` — Retrofit method annotated with `@GET("system/ping")`
  * `PingResponse` data class — deserializes JSON
  * `UserRepository.healthPing()` — calls ping endpoint
  * `AuthViewModel.ping()` — triggers ping as needed
  * Ping triggered before navigation to `AuthScreen` from `HomeScreen` (failures allowed)

---

## 🔗 Dependencies

* Jetpack Compose, Material3
* Navigation Compose
* Room (with Kotlin extensions)
* Jsoup (HTML scraping)
* Gson (JSON parsing)
* Kotlin Coroutines
* AndroidX Libraries

---
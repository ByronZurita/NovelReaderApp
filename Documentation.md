# NovelReaderApp – Android Studio Kotlin Project Documentation 

## Vision

* NovelReaderApp is a personal project mobile app for reading web novels using scrapers.
* Inspired by [Bernso/NovelReaderWeb](https://github.com/Bernso/NovelReaderWeb), [NovelReaderWeb](https://bernso.pythonanywhere.com).
* Backend API using Node.js, Express, MongoDB for data persitance [Byron-Backend](https://github.com/ByronDZH/Byron-Backend).
* Inspiration for EPUB Reader [AquileReader].
* Similar project for comparations [AkashicRecords](https://github.com/Luiz-eduardp/akashic_records).
* Similar project for comparations [QuickNovel](https://github.com/LagradOst/QuickNovel).
* Similar project for comparations [LNReader](https://github.com/LNReader/lnreader).

## Current State

### Scrapers Available

* RoyalRoad (EN)
* NovelBin (EN) — updated scraper system
* NovelasLigera (ES) — categories (CN/KR/JP), search, chapters

### 🧭 Screens (Updated)

* Home Screen
    Top bar: app title, settings icon, auth/profile button
    Source grid for scrapers
    Saved EPUBs section
    Language filters

* Scraper Screens
    Each scraper has:
    Search bar
    Pagination (Next/Previous)
    Latest releases / Best rated
    Source-specific filters (e.g. NovelBin category toggle)

* Novel Screen
    Cover, title, author
    Tags/genres
    Description (expandable)

* Chapter list
    Chapter Screen
    Custom TopBar: chapter title + settings button
    Previous/Next chapter buttons
    Font size scaling
    TTS integration
    HTML rendering support

* Settings Screen
    Custom colored top bar (new!)
    Font size slider
    TTS start/stop that pops back to chapter
    Theme controls (planned)

* Authentication Screen
    Register / Login
    Shows user novels from backend
    CRUD (create, edit, delete saved novels)

### Features Implemented

* Basic live mode
* Jetpack Compose + Material3 UI
* MVVM architecture
* Basic live updates
* Search functionality for RoyalRoad & NovelBin
* Backend integration: login, register, CRUD for saved novels
* Token persistence + encrypted storage
* TTS (play/stop) + font size adjustments
* Pagination support for scrapers
* Chapter navigation cleanup & bug fixes
* Dedicated SettingsViewModel for all reading settings
* Updated Settings TopBar with custom background color

### In Progress / Planned

* UI rework & design cohesion
* Theme selection (Dark/Light/custom palettes)
* Error-handling overhaul
* Favorites & bookmarks
* Reading progress tracking
* Advanced TTS settings (pitch, speed, auto-scroll)

---

## Project Structure

```
📁 backendconnection                       # Handles server communication and API calls
├── Repository.kt                          # saveToken(), getToken(), clearToken(), register(), login(), healthPing(), getUserNovels(), createNovel(), updateNovel(), deleteNovel()
├── RetrofitApi.kt                         # Defines API interface (ByronApi)
├── RetrofitClient.kt                      # Builds Retrofit instance with BASE_URL + Gson, exposes ByronApi singleton
│
├── 📁 backendmodels                       # Data models for backend communication
│   ├── AuthResponse.kt                    # (token:String) -> JWT token returned on successful login
│   ├── NovelBackend.kt                    # (@SN "_id":String?=null, title:String, author:String, status:String, chapters:Int, totalChapters:Int, notes:String, userId:String="")
│   └── UserCredentials.kt                 # (username:String, password:String)
│
📁 data                                    # Local app models and data structures
├── 📁 models
│   ├── Novel.kt                           # (id:String, title:String, alternativeTitle:String?=null, status:String?=null, author:String, genre:List<String>=emptyList(), description:String, url:String, tags:List<String>=emptyList(), sourceId:String, coverUrl:String?=null)
│   ├── Chapter.kt                         # (url:String, title:String, content:String?=null, novelUrl:String)
│   └── ChapterJson.kt                     # JS parsing support (e.g., window.chapters[] from RoyalRoad) # (id:Int, volumeId:Int, title:String, slug:String, date:String, order:Int, visible:Int, subscriptionTiers:Any?, doesNotRollOver:Boolean, isUnlocked:Boolean, url:String)                      
│
├── 📁 scraper                             # Web scrapers for novel sources
│   ├── 📁 base                            # Interfaces and factories used by scrapers
│   │   ├── NovelScraper.kt                # Interface defining fetchNovels(), fetchNovelChapters(), fetchChapterContent(), fetchNovelDetails() for scraper sources
│   │   └── ScraperFactory.kt              # Returns proper NovelScraper (RoyalRoad, NovelBin, NovelasLigera) based on source identifier
│   ├── NovelasLigeraScraper.kt                 # Scraper for novelasligera.com; fetchChineseNovels(), fetchKoreanNovels(), fetchJapaneseNovels(), fetchNovels(), fetchNovelDetails(), fetchNovelChapters(), fetchChapterContent()
│   ├── NovelBinScraper.kt                 # Scraper for novelbin.me; fetchNovelsPage(), fetchPopularNovelsPage(), searchNovels(), fetchNovels(), fetchNovelDetails(), fetchNovelChapters(), fetchChapterContent()
│   └── RoyalRoadScraper.kt                # Scraper for royalroad.com; fetchNovelsPage(), searchNovels(), fetchNovels(), fetchNovelChapters(), fetchChapterContent(), getBestRatedNovels(), fetchNovelDetails()
│
📁 ui                                      # UI layer (Jetpack Compose screens and components)
├── 📁 components                          # Reusable UI widgets NovelCard.kt
│   
├── 📁 screens                             # App screens
│   ├── 📁 common                          # Shared UI components and screens
│   │   └── AppNavigation.kt               # Defines app navigation graph and routes; HomeScreen(), NovelsScreen(), ChapterListScreen(), ChapterHostScreen(), AuthScreen(), SettingsScreen()
│   ├── AuthScreen.kt                  # Handles user authentication (login/register), displays user novels, allows adding/editing/deleting novels, and manages auth state with reactive Compose UI.
│   ├── SettingsScreen.kt              # Allows adjusting font size via slider and controlling TTS playback, reflecting ViewModel state in real-time
│   ├── ChapterContentScreen.kt            # Displays chapter content with HTML rendering, supports font scaling, TTS, and chapter navigation with Previous/Next controls
│   ├── ChapterListScreen.kt               # Displays novel details (cover, author, tags, description) and a scrollable list of chapters with expandable description and chapter click handling
│   ├── HomeScreen.kt                      # Main home screen showing welcome card, language filters, and a grid of available scraper sources with navigation actions
│   └── NovelListScreen.kt                 # Composable screen to display novels from multiple sources with filters, search, and pagination
│
├── 📁 theme                              # 
│   ├── Color.kt                          # Full updated palette Steel1,2,3,4 and Violet1,2,3,4 = Color(0x--------)
│   ├── Theme.kt                          # ColorSchemes + NovelReaderAppTheme
│   └── Type.kt                           # Typography: bodyLarge, titleLarge
│
📁 viewmodel                               # ViewModel layer (MVVM pattern)
├── AuthViewModel.kt                       # Manages authentication, JWT parsing, user session, and user novel CRUD operations
├── ChapterViewModel.kt                    # Manages novel and chapter data; loadChapters(), loadChapter(), tracks title, author, description, tags, coverUrl, chapterContent
├── ScrapersViewmodels.kt                  # Contains NovelasLigeraViewmodel; fetchNovelsForCategory(), fetchNextPage(), loadNextPage(), loadPreviousPage(), selectCategory() | NovelBinViewmodel; loadNovelsPage(), loadNextPage(), loadPreviousPage(), toggleCategory(), toggleCompleted(), applyFilter(), searchNovels(), updateSearchQuery() | RoyalRoadViewModel ; loadNovelsPage(), loadBestRatedNovels(), toggleBestRatedMode(), updateGenre(), searchNovels(), updateSearchQuery().
├── SettingsViewModel.kt                   # Manages user settings and TTS playback; htmlContent(), fontSize(), setFontSize(), startTTSAsync(), toggleTTS(), stopTTS()
└── ViewModelFactory.kt                    # Factory to create AuthViewModel instances with Application and UserRepository

MainActivity.kt                            # Entry point of the app (sets up navigation and theme)
```

---

## Dependencies

| Category               | Libraries / Dependencies               | Role                              |
| ---------------------- | -------------------------------------- | --------------------------------- |
| Image Loading          | Coil Compose                           | Novel cover display               |
| Security               | Security Crypto                        | JWT / credentials encryption      |
| Networking             | Retrofit, Gson, OkHttp                 | API calls & parsing               |
| Asynchronous           | Kotlin Coroutines                      | Background tasks                  |
| UI / Compose           | Compose, Material3, Icons, Accompanist | All screens, layouts, themes      |
| Navigation             | Navigation Compose                     | Screen routing                    |
| Local Persistence      | Room, DataStore                        | Cache, settings, reading progress |
| Scraping / Parsing     | Jsoup                                  | Extract novels & chapters         |
| Legacy / Compatibility | RecyclerView, AppCompat, Material      | Optional legacy support           |
| Testing                | JUnit, Espresso, Compose testing       | Unit & UI testing                 |

---

## Backend API Features Added to the App

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
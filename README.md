# 📱 Şu an Meşgulüm (I'm Busy Right Now)

![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue?logo=kotlin)
![Min SDK](<https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-brightgreen>)
![Target SDK](<https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-brightgreen>)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-orange)
![License](https://img.shields.io/badge/License-MIT-yellow)

**Şu an Meşgulüm**, gelen aramaları otomatik olarak sesli mesajla yanıtlayan bir Android uygulamasıdır. Kullanıcı tarafından tanımlanan "meşguliyet modları" ile arayan kişiye TTS (Text-to-Speech) aracılığıyla sesli yanıt verilir.

_An Android application that automatically answers incoming phone calls with a voice message using TTS (Text-to-Speech). Users define custom "busy modes" and the app plays the corresponding message to the caller._

---

## ✨ Features / Özellikler

### Core / Temel

- 📞 **Auto-answer incoming calls** with TTS voice messages / Gelen aramaları TTS sesli mesajla otomatik yanıtlama
- 🎭 **Custom busy modes** — create, edit, delete, set default / Özel meşguliyet modları oluşturma
- 🔔 **Notification quick-select** — choose a mode from the notification when a call arrives / Arama geldiğinde bildirimden hızlı mod seçimi
- 📋 **Call log** — track all auto-answered calls with mode info / Tüm yanıtlanan aramaların kaydı
- 🌍 **Turkish & English** language support / Türkçe ve İngilizce dil desteği
- 🔇 **Silent reject** — reject calls without playing any message / Sessiz reddetme modu

### Premium

- 🗣️ **Chatterbox TTS** — premium, natural-sounding voice engine powered by ONNX Runtime / Premium doğal sesli TTS motoru
- 😄 **Paralinguistic tags** — add emotions like `[laugh]`, `[sigh]`, `[whisper]` to your messages / Mesajlara duygu ifadeleri ekleme
- 💎 **One-time purchase** via Google Play Billing / Google Play üzerinden tek seferlik satın alma

### Technical

- 🏗️ **MVVM + Clean Architecture** with clear separation of concerns
- 💉 **Dagger Hilt** dependency injection
- 🗄️ **Room Database** for persistent storage
- 📡 **CallScreeningService** (Android 10+) for call interception
- 🔄 **Foreground Service** for persistent background operation
- 🧭 **Jetpack Navigation Component** with bottom navigation
- 🎨 **Material Design** components and theming

### v2: AI Call Assistant / Yapay Zeka Çağrı Asistanı

- 🤖 **AI Call Screening** — assistant answers calls, has multi-turn dialogues with callers / Asistan aramaları cevaplar, arayanlarla çok turlu diyalog kurar
- 🎙️ **Vosk STT** — real-time speech-to-text for caller transcription / Arayan ses tanıma
- 📝 **Live Call Activity** — watch real-time transcript as assistant talks to caller / Gerçek zamanlı transkript izleme
- 📱 **3-Button Notification** — "Assistant Answer" / "Silent Reject" / "Ignore" / 3 butonlu bildirim
- 📬 **Voicemail** — callers can leave voice messages, stored with transcripts / Sesli mesaj bırakma ve transkript
- ⚙️ **Assistant Customization** — configure name, personality, greetings, farewell messages / Asistan kişiselleştirme
- 🗣️ **TTS Voice Selection** — preview and select from available TTS voices / Ses seçimi ve önizleme
- 🎵 **Spotify Integration** (stub) — prepared for future hold music / Bekleme müziği altyapısı

---

## 📐 Architecture / Mimari

```
com.suanmesgulum.app/
├── domain/                     # Domain Layer
│   ├── model/
│   │   ├── CustomMode.kt       # Busy mode data class
│   │   ├── CallLogItem.kt      # Call log data class
│   │   ├── CallSession.kt      # v2: Call session tracking
│   │   ├── CallMessage.kt      # v2: Individual messages in session
│   │   ├── Voicemail.kt        # v2: Voicemail recordings
│   │   ├── SpotifyTrack.kt     # v2: Music track (stub)
│   │   └── AssistantSettings.kt# v2: Assistant configuration
│   └── repository/
│       ├── CustomModeRepository.kt
│       ├── CallLogRepository.kt
│       ├── CallSessionRepository.kt    # v2
│       ├── VoicemailRepository.kt      # v2
│       └── AssistantSettingsRepository.kt # v2
├── data/                       # Data Layer
│   ├── local/
│   │   ├── entity/             # Room entities (7 total)
│   │   ├── dao/                # Room DAOs (7 total)
│   │   ├── AppDatabase.kt     # Room database v2 (with migration)
│   │   └── mapper/            # Entity ↔ Domain mapping
│   └── repository/            # Repository implementations
├── di/                        # Dependency Injection
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── TtsModule.kt
├── tts/                       # TTS Engine Layer
│   ├── TtsEngine.kt           # Engine interface
│   ├── AndroidTtsEngine.kt    # Built-in TTS (free)
│   ├── ChatterboxTtsEngine.kt # ONNX-based TTS (premium)
│   └── TtsManager.kt          # Engine selection manager
├── service/                   # Services & Receivers
│   ├── ServicePreferences.kt
│   ├── BusyCallScreeningService.kt  # v2: 3-button notification
│   ├── BusyForegroundService.kt
│   ├── TtsPlaybackService.kt
│   ├── CallActionReceiver.kt       # v2: +ASSISTANT_ANSWER, +IGNORE
│   ├── PhoneStateReceiver.kt       # v2: 3-button notification
│   ├── orchestrator/
│   │   └── CallOrchestratorService.kt  # v2: State machine
│   ├── stt/
│   │   ├── SpeechToTextManager.kt  # v2: Vosk STT
│   │   └── SttModule.kt
│   ├── audio/
│   │   ├── AudioPlayerManager.kt   # v2: Audio playback
│   │   ├── MusicPlayer.kt          # v2: Interface
│   │   └── AudioModule.kt
│   ├── spotify/
│   │   ├── SpotifyManager.kt       # v2: Stub
│   │   └── SpotifyModule.kt
│   └── voicemail/
│       ├── VoicemailManager.kt     # v2
│       └── VoicemailModule.kt
├── billing/
│   └── BillingManager.kt      # Google Play Billing
├── presentation/              # UI Layer
│   ├── main/                  # MainActivity
│   ├── dashboard/             # Dashboard screen (v2: +assistant buttons)
│   ├── modes/                 # Modes CRUD screen
│   ├── logs/                  # Call logs screen
│   ├── settings/              # Settings screen
│   ├── modeselect/           # Quick mode select dialog
│   ├── incomingcall/         # Incoming call full-screen
│   ├── livecall/             # v2: Live call transcript screen
│   ├── assistant/            # v2: Assistant customize (ViewPager2)
│   └── voicemail/            # v2: Voicemail list & playback
└── ImBusyApplication.kt      # @HiltAndroidApp (v2: 5 channels)
```

---

## 🖥️ Screens / Ekranlar

| Screen                     | Description                                                          |
| -------------------------- | -------------------------------------------------------------------- |
| **Dashboard**              | Service toggle, today's call stats, assistant buttons, quick nav     |
| **Modes**                  | Create/edit/delete busy modes, set default mode, reorder             |
| **Call Logs**              | List of auto-answered calls with date, caller info, mode used        |
| **Settings**               | Auto-answer toggle, language selection (TR/EN), premium purchase     |
| **Mode Select**            | Dialog-style activity launched from notification for quick mode pick |
| **Live Call** (v2)         | Real-time transcript of assistant-caller dialogue, continue/reject   |
| **Assistant Customize** (v2) | ViewPager2 with 3 tabs: General, Messages, Voices                 |
| **Voicemail** (v2)         | List voicemails with playback, transcript, archive, delete           |

---

## 🛠️ Tech Stack / Teknoloji Yığını

| Category              | Technology          | Version            |
| --------------------- | ------------------- | ------------------ |
| Language              | Kotlin              | 1.9.22             |
| Build                 | Gradle (KTS)        | 8.5                |
| Android Gradle Plugin | AGP                 | 8.2.2              |
| DI                    | Dagger Hilt         | 2.50               |
| Database              | Room                | 2.6.1              |
| Navigation            | Jetpack Navigation  | 2.7.7              |
| Async                 | Coroutines + Flow   | 1.7.3              |
| Billing               | Google Play Billing | 6.1.0              |
| ML Runtime            | ONNX Runtime        | 1.16.3             |
| UI                    | Material Components | 1.11.0             |
| Annotation Processing | KSP                 | 1.9.22-1.0.17      |
| Min SDK               | API 26              | Android 8.0 (Oreo) |
| Target SDK            | API 34              | Android 14         |

---

## 🚀 Setup / Kurulum

### Prerequisites / Ön Koşullar

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17**
- **Android SDK** API 34 installed
- A physical Android device (call features don't work on emulators)

### Build Steps / Derleme Adımları

```bash
# 1. Clone the repository / Repoyu klonlayın
git clone https://github.com/yourusername/suan-mesgulum.git
cd suan-mesgulum

# 2. Open in Android Studio / Android Studio'da açın
# File → Open → select the project root folder

# 3. Sync Gradle / Gradle senkronizasyonu
# Android Studio will automatically sync. If not:
# File → Sync Project with Gradle Files

# 4. Build the project / Projeyi derleyin
./gradlew assembleDebug

# 5. Install on device / Cihaza yükleyin
./gradlew installDebug
```

### Signing / İmzalama

For release builds, create a `keystore.jks` and configure signing in `app/build.gradle.kts`:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("keystore.jks")
        storePassword = "your_store_password"
        keyAlias = "your_key_alias"
        keyPassword = "your_key_password"
    }
}
```

---

## 📖 Usage / Kullanım

### First Launch / İlk Açılış

1. **Grant Permissions** — The app will request:
   - Phone state & call answering
   - Contacts (for caller name display)
   - Call log reading
   - Notification permission (Android 13+)

2. **Set as Call Screening App** — Required for intercepting incoming calls. The app will prompt you to set it as the default call screening service.

3. **Create Busy Modes** — Navigate to the Modes tab and create your first mode:
   - **Name**: e.g., "Toplantıdayım" (In a meeting)
   - **Text**: e.g., "Şu an toplantıdayım, daha sonra arayacağım." (I'm in a meeting, I'll call back later.)

4. **Enable the Service** — Toggle the service ON from the Dashboard.

### How It Works / Nasıl Çalışır

```
📞 Incoming Call
    │
    ├─ Auto-answer OFF ──→ 🔔 Notification with mode buttons
    │                           ├─ [Mode 1] [Mode 2] [Mode 3]
    │                           └─ [Silent Reject]
    │
    └─ Auto-answer ON ───→ 🤖 Default mode TTS plays automatically
                                ├─ Call answered via TelecomManager
                                ├─ Audio routed to VOICE_COMMUNICATION
                                ├─ TTS synthesized → MediaPlayer plays
                                └─ Call ended, log saved to database
```

### Paralinguistic Tags (Premium) / Duygu İfadeleri

With the premium Chatterbox TTS engine, you can add emotion tags to your messages:

| Tag         | Description         |
| ----------- | ------------------- |
| `[laugh]`   | Laughter / Gülme    |
| `[cough]`   | Cough / Öksürük     |
| `[sigh]`    | Sigh / İç çekme     |
| `[gasp]`    | Gasp / Nefes nefese |
| `[cry]`     | Crying / Ağlama     |
| `[whisper]` | Whisper / Fısıltı   |
| `[shout]`   | Shout / Bağırma     |

**Example / Örnek:**

```
[sigh] Şu an çok meşgulüm, lütfen daha sonra arayın. [laugh] Şaka şaka, ama gerçekten meşgulüm.
```

---

## 💎 Premium Voice Pack / Premium Ses Paketi

The app includes a free Android TTS engine that works out of the box. For a more natural and expressive voice experience, you can unlock the **Chatterbox TTS** engine via a one-time in-app purchase.

| Feature             | Free (Android TTS) | Premium (Chatterbox) |
| ------------------- | :----------------: | :------------------: |
| Basic TTS           |         ✅         |          ✅          |
| Turkish & English   |         ✅         |          ✅          |
| Speech rate control |         ✅         |          ✅          |
| Pitch control       |         ✅         |          ✅          |
| Natural voice       |         ❌         |          ✅          |
| Paralinguistic tags |         ❌         |          ✅          |
| ONNX-powered        |         ❌         |          ✅          |

**Product ID:** `premium_voice_pack`

---

## 📁 Project Structure / Proje Yapısı

```
020-imbusy/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/suanmesgulum/app/
│           │   ├── ImBusyApplication.kt
│           │   ├── domain/
│           │   ├── data/
│           │   ├── di/
│           │   ├── tts/
│           │   ├── service/
│           │   ├── billing/
│           │   └── presentation/
│           └── res/
│               ├── layout/          (10 XML layouts)
│               ├── drawable/        (14 vector drawables)
│               ├── mipmap-anydpi-v26/ (adaptive icons)
│               ├── navigation/      (nav_graph.xml)
│               ├── menu/            (bottom_nav_menu.xml)
│               ├── color/           (bottom_nav_color.xml)
│               ├── values/          (colors, strings TR, dimens, themes)
│               └── values-en/       (strings EN)
└── README.md
```

---

## 🔐 Permissions / İzinler

| Permission                      | Purpose                            |
| ------------------------------- | ---------------------------------- |
| `READ_PHONE_STATE`              | Detect incoming calls              |
| `ANSWER_PHONE_CALLS`            | Answer calls programmatically      |
| `MANAGE_OWN_CALLS`              | Manage call routing                |
| `READ_CONTACTS`                 | Display caller name in logs        |
| `READ_CALL_LOG`                 | Access call history                |
| `RECORD_AUDIO`                  | Required for call audio routing    |
| `FOREGROUND_SERVICE`            | Keep service running               |
| `FOREGROUND_SERVICE_PHONE_CALL` | Phone call type foreground service |
| `POST_NOTIFICATIONS`            | Show call notifications            |
| `INTERNET`                      | Premium model download             |
| `ACCESS_NETWORK_STATE`          | Check connectivity                 |

---

## 🧪 Testing / Test

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented tests
./gradlew connectedDebugAndroidTest

# Build and lint check
./gradlew lintDebug
```

For testing in-app purchases without a Play Console, use the `BillingManager.simulatePurchase()` method in debug builds.

---

## 🔮 Roadmap / Yol Haritası

- [ ] Widget for quick service toggle
- [ ] Scheduled modes (time-based auto-activation)
- [ ] Contact-specific mode assignment
- [ ] Wear OS companion app
- [ ] Call recording option
- [ ] WhatsApp / Telegram integration
- [ ] Mode sharing between users
- [ ] Dark theme support

---

## 🤝 Contributing / Katkıda Bulunma

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

## 📄 License / Lisans

This project is licensed under the **MIT License**.

```
MIT License

Copyright (c) 2024 Şu an Meşgulüm

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 📞 Contact / İletişim

For questions, issues, or feature requests, please open an issue on GitHub.

---

<p align="center">
  Made with ❤️ in Turkey / Türkiye'de ❤️ ile yapıldı
</p>

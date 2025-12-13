# Firebase Auth Flow - Dokumentacja

## 🔥 Zaimplementowane Flow Autentykacji

### Architektura
```
SplashActivity (Entry Point)
    ├─→ Niezalogowany → LoginActivity
    │                      ├─→ Rejestracja → OnboardingActivity → UserMainActivity
    │                      └─→ Logowanie → SplashActivity (recheck) → UserMainActivity
    │
    └─→ Zalogowany → Sprawdź status w API
                        ├─→ Ma dane → UserMainActivity
                        └─→ Brak danych → OnboardingActivity → UserMainActivity
```

## 📁 Nowe Pliki

### 1. **AuthManager.kt**
Singleton do zarządzania Firebase Authentication.

**Główne metody:**
- `currentUserId: String?` - Zwraca UID zalogowanego użytkownika
- `isUserLoggedIn(): Boolean` - Sprawdza status logowania
- `registerWithEmail(email, password)` - Rejestracja nowego użytkownika
- `loginWithEmail(email, password)` - Logowanie
- `logout()` - Wylogowanie
- `sendPasswordResetEmail(email)` - Reset hasła

**Użycie:**
```kotlin
// Pobierz ID aktualnego użytkownika
val userId = AuthManager.currentUserId

// Sprawdź czy zalogowany
if (AuthManager.isUserLoggedIn()) {
    // ...
}

// Wyloguj
AuthManager.logout()
```

### 2. **SplashActivity.kt**
Entry point aplikacji - decyduje o routingu.

**Logika:**
1. Sprawdza czy użytkownik jest zalogowany w Firebase
2. Jeśli NIE → przekierowanie do `LoginActivity`
3. Jeśli TAK → pobiera dane z API:
   - Ma kompletne dane → `UserMainActivity`
   - Brak danych/błąd → `OnboardingActivity`

### 3. **LoginActivity.kt**
Ekran logowania i rejestracji.

**Funkcjonalności:**
- Przełączanie między trybem logowania/rejestracji
- Walidacja email/hasła
- Obsługa błędów Firebase (w języku polskim)
- Reset hasła (wysyłka emaila)
- Loading state podczas operacji

**Flow rejestracji:**
```
Rejestracja → OnboardingActivity → Wypełnienie danych → POST /api/users → UserMainActivity
```

**Flow logowania:**
```
Logowanie → SplashActivity → Sprawdzenie statusu → UserMainActivity
```

## 🔧 Zmodyfikowane Pliki

### **UserGoalsFragment.kt**
- ✅ Używa `AuthManager.currentUserId` do pobrania Firebase UID
- ✅ Tworzy użytkownika z `firebaseUid` zamiast `null`
- ✅ Ustawia `provider = "firebase"` zamiast `"local"`

**Przed:**
```kotlin
auth = AuthData(
    provider = "local",
    firebaseUid = null
)
```

**Po:**
```kotlin
val firebaseUid = AuthManager.currentUserId
auth = AuthData(
    provider = "firebase",
    firebaseUid = firebaseUid
)
```

### **UserProfileFragment.kt**
- ✅ Używa `AuthManager.currentUserId` zamiast hardcoded ID
- ✅ Dodano przycisk "Wyloguj się"
- ✅ Dodano funkcjonalność wylogowania z dialogiem potwierdzenia

### **AndroidManifest.xml**
- ✅ `SplashActivity` jako LAUNCHER (entry point)
- ✅ `MainActivity` bez LAUNCHER (opcjonalnie do testów)
- ✅ Dodano `LoginActivity` i `SplashActivity`

## 🎯 Kolejne Kroki - Usunięcie Hardcoded IDs

Wyszukaj i zastąp we wszystkich plikach:
```kotlin
// PRZED
private val currentUserId = "68cbc06e6cdfa7faa8561f82"

// PO
private val currentUserId: String
    get() = AuthManager.currentUserId ?: ""
```

### Pliki do aktualizacji:
1. ✅ **UserProfileFragment.kt** - ZROBIONE
2. ⏳ **UserMainActivity.kt** - line 58
3. ⏳ **UserDiaryFragment.kt** - line 72
4. ⏳ **UserTrainingFragment.kt** - line 42
5. ⏳ **UserWeightFragment.kt** - line 77, 380
6. ⏳ **UserProgressFragment.kt** - line 34
7. ⏳ **ProgressUniversalListDialogFragment.kt** - line 41

## 🔐 Backend Integration

Twój backend musi:
1. ✅ Weryfikować token Firebase (już masz w `authMiddleware.js`)
2. ✅ Akceptować `firebaseUid` w `CreateUserDto`
3. ✅ Endpoint `GET /api/users/:id` używa Firebase UID jako `:id`

**Przykład requestu z tokenem:**
```
GET /api/users/abc123firebaseuid
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

Backend ekstraktuje `req.user.uid` z tokena i weryfikuje czy `req.user.uid === req.params.id`.

## 🧪 Testowanie

### 1. Nowa rejestracja
1. Uruchom aplikację → `SplashActivity` → `LoginActivity`
2. Przełącz na "Zarejestruj się"
3. Wprowadź email/hasło (min. 6 znaków)
4. Potwierdź hasło
5. Kliknij "Zarejestruj się"
6. → Przekierowanie do `OnboardingActivity`
7. Wypełnij dane (waga, wzrost, cel)
8. → POST do `/api/users` z Firebase UID
9. → Przekierowanie do `UserMainActivity`

### 2. Logowanie istniejącego użytkownika
1. Uruchom aplikację → `SplashActivity` → `LoginActivity`
2. Wprowadź email/hasło
3. Kliknij "Zaloguj się"
4. → Sprawdzenie w API (GET /api/users/:firebaseUid)
5. → Przekierowanie do `UserMainActivity`

### 3. Wylogowanie
1. W aplikacji → Profil
2. Kliknij "Wyloguj się"
3. Potwierdź
4. → Przekierowanie do `LoginActivity`

### 4. Reset hasła
1. Na ekranie logowania
2. Wprowadź email
3. Kliknij "Nie pamiętam hasła"
4. → Wysłany email z linkiem resetującym

## 🐛 Znane problemy / TODO

- [ ] Zastąp hardcoded IDs w pozostałych fragmentach
- [ ] Dodaj obsługę Google Sign-In (opcjonalnie)
- [ ] Obsługa offline (cache danych użytkownika)
- [ ] Lepszy splash screen z animacją
- [ ] Walidacja siły hasła
- [ ] Obsługa weryfikacji emaila

## 📝 Notatki

- **NetworkModule.kt** już ma `AuthInterceptor`, który automatycznie dodaje token do każdego requestu
- Token Firebase jest ważny przez 1 godzinę i automatycznie odświeżany
- `AuthManager` używa suspend functions dla async operacji
- Wszystkie błędy Firebase są tłumaczone na polski w `LoginActivity`

---

**Ostatnia aktualizacja:** 13 grudnia 2025
**Autor:** Kamil Knapik

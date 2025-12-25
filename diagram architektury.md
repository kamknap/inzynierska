flowchart LR
    %% Definicja systemów zewnętrznych
    F[Firebase Auth]
    OFF[OpenFoodFacts API]
    YT[YouTube]

    %% Urządzenie mobilne
    subgraph D [Urządzenie mobilne Android]
        direction TB
        A[Aplikacja mobilna]
        HC[Health Connect]
        A <-->|API lokalne| HC
    end

    %% Backend
    subgraph C [Chmura Azure / Docker]
        S[REST API Node.js + Express]
    end

    %% Baza danych
    subgraph M [Klaster MongoDB Atlas]
        DB[(Baza Danych)]
    end

    %% Połączenia główne
    A <-->|HTTPS / JSON| S
    S <-->|Mongoose / TLS| DB

    %% Integracje z Aplikacji (zgodnie z Twoim opisem)
    A -.->|1. Logowanie| F
    A -.->|2. Token w nagłówku| S
    S -.->|3. Weryfikacja tokenu| F
    
    A -->|Pobierz dane produktu| OFF
    A -->|Odtwarzanie wideo| YT

```mermaid
flowchart LR
  subgraph D[Urządzenie mobilne (Android)]
    A[Aplikacja mobilna]
    HC[Health Connect (Android)]
    A <-->|API lokalne| HC
  end

  subgraph C[Chmura (Azure / VM / App Service)]
    S[REST API (Node.js + Express)\nDocker]
  end

  subgraph M[Chmura (MongoDB Atlas)]
    DB[(MongoDB)]
  end

  F[Firebase Authentication]
  OFF[OpenFoodFacts API]
  YT[YouTube]

  A <-->|HTTPS + JSON (REST)| S
  S -->|Mongoose / TLS| DB

  A -->|logowanie / token| F
  S -->|weryfikacja tokenu| F

  S -->|pobierz dane produktu| OFF

  A -->|odtwarzanie wideo| YT
```

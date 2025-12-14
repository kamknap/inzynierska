# 🎨 System Kolorów i Stylów - FitHub App

## 📋 Spis treści
1. [Kolory](#kolory)
2. [Style](#style)
3. [Wymiary](#wymiary)
4. [Najlepsze praktyki](#najlepsze-praktyki)

---

## 🎨 Kolory

### Główne kolory aplikacji

#### 🟣 Fioletowy (Primary)
Główny kolor aplikacji - używany dla:
- Przycisków głównych (CTA - Call To Action)
- Bottom Navigation (zaznaczony element)
- Nagłówków
- Akcentów UI

```xml
@color/purple_primary          #7C4DFF (główny)
@color/purple_primary_dark     #651FFF (ciemny wariant)
@color/purple_primary_light    #B388FF (jasny wariant)
@color/purple_primary_lighter  #D1C4E9 (bardzo jasny)
@color/purple_primary_very_light #EDE7F6 (tła, kontenery)
```

#### 🟢 Zielony (Success/Health)
Kolor sukcesu - używany dla:
- Pozytywnych akcji (zapisz, potwierdź)
- Progresu i osiągnięć
- Zdrowych wyborów w diecie
- Odznak i challengy

```xml
@color/green_success           #4CAF50 (główny)
@color/green_success_dark      #388E3C (ciemny wariant)
@color/green_success_light     #81C784 (jasny wariant)
@color/green_success_lighter   #C8E6C9 (bardzo jasny)
@color/green_success_very_light #E8F5E9 (tła)
```

#### 🔵 Niebieski (Info)
Kolor informacyjny - używany dla:
- Linków
- Wykresów i statystyk
- Informacji pomocniczych
- Oznaczenia treningów

```xml
@color/blue_info               #2196F3 (główny)
@color/blue_info_dark          #1976D2 (ciemny wariant)
@color/blue_info_light         #64B5F6 (jasny wariant)
@color/blue_info_lighter       #BBDEFB (bardzo jasny)
@color/blue_info_very_light    #E3F2FD (tła)
```

### Kolory semantyczne

```xml
@color/error        #F44336 (błędy)
@color/warning      #FF9800 (ostrzeżenia)
```

### Kolory tekstu

```xml
@color/text_primary    #212121 (główny tekst)
@color/text_secondary  #757575 (tekst pomocniczy)
@color/text_tertiary   #9E9E9E (tekst mniej ważny)
@color/text_disabled   #BDBDBD (tekst nieaktywny)
```

---

## 🎭 Style

### Przyciski

#### Główny przycisk (wypełniony)
```xml
<Button
    style="@style/Widget.Fithub.Button"
    android:text="Zapisz" />
```

#### Przycisk sukcesu (zielony)
```xml
<Button
    style="@style/Widget.Fithub.Button.Success"
    android:text="Potwierdź" />
```

#### Przycisk informacyjny (niebieski)
```xml
<Button
    style="@style/Widget.Fithub.Button.Info"
    android:text="Więcej info" />
```

#### Przycisk obramowany
```xml
<Button
    style="@style/Widget.Fithub.Button.Outlined"
    android:text="Anuluj" />
```

#### Przycisk tekstowy
```xml
<Button
    style="@style/Widget.Fithub.Button.Text"
    android:text="Pomiń" />
```

### Karty (CardView)

#### Standardowa karta z cieniem
```xml
<androidx.cardview.widget.CardView
    style="@style/Widget.Fithub.CardView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    
    <!-- Zawartość karty -->
    
</androidx.cardview.widget.CardView>
```

#### Karta z obramowaniem
```xml
<androidx.cardview.widget.CardView
    style="@style/Widget.Fithub.CardView.Outlined"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    
    <!-- Zawartość karty -->
    
</androidx.cardview.widget.CardView>
```

### Pola tekstowe

```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.Fithub.TextInputLayout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Nazwa">
    
    <com.google.android.material.textfield.TextInputEditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
        
</com.google.android.material.textfield.TextInputLayout>
```

### Style tekstu

```xml
<!-- Nagłówek 1 (największy) -->
<TextView
    style="@style/TextAppearance.Fithub.Headline1"
    android:text="Główny tytuł" />

<!-- Nagłówek 2 -->
<TextView
    style="@style/TextAppearance.Fithub.Headline2"
    android:text="Podtytuł" />

<!-- Nagłówek 3 -->
<TextView
    style="@style/TextAppearance.Fithub.Headline3"
    android:text="Sekcja" />

<!-- Tekst body (normalny) -->
<TextView
    style="@style/TextAppearance.Fithub.Body1"
    android:text="Główny tekst" />

<!-- Tekst body (mniejszy) -->
<TextView
    style="@style/TextAppearance.Fithub.Body2"
    android:text="Tekst pomocniczy" />

<!-- Caption (najmniejszy) -->
<TextView
    style="@style/TextAppearance.Fithub.Caption"
    android:text="Drobny tekst" />
```

---

## 📏 Wymiary

### Odstępy (spacing)

Używaj standardowych odstępów dla spójności:

```xml
@dimen/spacing_tiny      4dp   (bardzo małe odstępy)
@dimen/spacing_small     8dp   (małe odstępy)
@dimen/spacing_medium    12dp  (średnie odstępy)
@dimen/spacing_normal    16dp  (normalne odstępy - DOMYŚLNE)
@dimen/spacing_large     24dp  (duże odstępy)
@dimen/spacing_xlarge    32dp  (bardzo duże)
@dimen/spacing_xxlarge   48dp  (ogromne)
```

**Przykład użycia:**
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="@dimen/spacing_normal"
    android:layout_margin="@dimen/spacing_medium">
```

### Właściwości kart

```xml
@dimen/card_corner_radius    16dp
@dimen/card_elevation        4dp
@dimen/card_padding          16dp
@dimen/card_margin           12dp
```

### Rozmiary tekstu

```xml
@dimen/text_size_headline1   32sp
@dimen/text_size_headline2   24sp
@dimen/text_size_headline3   20sp
@dimen/text_size_body1       16sp (domyślny)
@dimen/text_size_body2       14sp
@dimen/text_size_caption     12sp
```

---

## ✅ Najlepsze praktyki

### 1. Zawsze używaj zasobów (resources)

❌ **ŹLE:**
```xml
<TextView
    android:textColor="#000000"
    android:textSize="16sp" />
```

✅ **DOBRZE:**
```xml
<TextView
    android:textColor="@color/text_primary"
    android:textSize="@dimen/text_size_body1" />
```

### 2. Używaj stylów dla powtarzających się elementów

❌ **ŹLE:**
```xml
<Button
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:textColor="@color/white"
    android:backgroundTint="@color/purple_primary"
    android:textSize="16sp"
    android:paddingHorizontal="24dp"
    android:text="Przycisk 1" />

<Button
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:textColor="@color/white"
    android:backgroundTint="@color/purple_primary"
    android:textSize="16sp"
    android:paddingHorizontal="24dp"
    android:text="Przycisk 2" />
```

✅ **DOBRZE:**
```xml
<Button
    style="@style/Widget.Fithub.Button"
    android:text="Przycisk 1" />

<Button
    style="@style/Widget.Fithub.Button"
    android:text="Przycisk 2" />
```

### 3. Wybieraj odpowiednie kolory do kontekstu

- **Fioletowy** → Główne akcje, nawigacja, akcenty
- **Zielony** → Sukces, potwierdzenia, pozytywne akcje, zdrowie
- **Niebieski** → Informacje, linki, wykresy
- **Czerwony** → Błędy, usuwanie, akcje destrukcyjne
- **Pomarańczowy** → Ostrzeżenia

### 4. Zachowuj hierarchię tekstu

```xml
<!-- Strona/Fragment -->
Headline1 (32sp, bold) → Tytuł strony/fragmentu
  ↓
Headline2 (24sp, bold) → Sekcje
  ↓
Headline3 (20sp, bold) → Podsekcje
  ↓
Body1 (16sp) → Główna treść
  ↓
Body2 (14sp) → Treść pomocnicza
  ↓
Caption (12sp) → Drobne informacje
```

### 5. Stosuj konsystentne odstępy

- **Padding wewnątrz kart**: `@dimen/card_padding` (16dp)
- **Margin między kartami**: `@dimen/card_margin` (12dp)
- **Padding ekranu**: `@dimen/spacing_normal` (16dp)
- **Odstępy między elementami**: `@dimen/spacing_medium` (12dp)

### 6. CardView - kiedy używać?

Używaj CardView dla:
- Grup powiązanych informacji
- Elementów listy (opcjonalnie)
- Sekcji wymagających wyróżnienia
- Interaktywnych elementów

### 7. Przyciski - wybór odpowiedniego typu

- **Widget.Fithub.Button** → Główna akcja (np. "Zapisz", "Dalej")
- **Widget.Fithub.Button.Success** → Pozytywna akcja (np. "Potwierdź")
- **Widget.Fithub.Button.Outlined** → Akcja wtórna (np. "Anuluj")
- **Widget.Fithub.Button.Text** → Akcja najmniej ważna (np. "Pomiń")

---

## 🔄 Migracja ze starych kolorów

Jeśli widzisz w kodzie stare kolory, zamień je zgodnie z poniższą tabelą:

| Stary kolor | Nowy kolor |
|-------------|------------|
| `@android:color/black` | `@color/text_primary` |
| `@android:color/holo_blue_bright` | `@color/blue_info` |
| `#000000` | `@color/text_primary` |
| Twarde hex kolory | Odpowiednie `@color/...` |

---

## 📞 Pytania?

Jeśli masz wątpliwości, który kolor lub styl użyć:
1. Sprawdź ten dokument
2. Zobacz jak wygląda w innych fragmentach
3. Pytaj, jeśli coś jest niejasne

**Konsystencja > Kreatywność** 🎯

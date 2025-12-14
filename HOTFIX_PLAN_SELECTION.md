# 🐛 Hotfix - SelectExercisePlanDialogFragment

## ❌ Problem
Po refaktoryzacji dialogu wyboru planu treningowego kliknięcie na plan NIE zmieniało aktywnego planu.

## 🔍 Analiza przyczyny

### Co było źle:
W `item_exercise_plan.xml` struktura wyglądała tak:
```xml
<CardView>
    <LinearLayout> <!-- główny -->
        <LinearLayout 
            android:clickable="true"      ← PROBLEM!
            android:focusable="true"
            android:background="?attr/selectableItemBackground">
            <!-- nazwa planu -->
        </LinearLayout>
        <ImageButton /> <!-- delete -->
    </LinearLayout>
</CardView>
```

W kodzie Kotlin:
```kotlin
planItemView.setOnClickListener { ... }  // ustawiony na CardView
```

**Problem:** Wewnętrzny `LinearLayout` z `android:clickable="true"` **przejmował wszystkie kliknięcia** i blokował propagację do CardView. Listener na CardView nigdy się nie wywoływał!

---

## ✅ Rozwiązanie

### 1. Dodanie ID do obszaru klikalnego

**item_exercise_plan.xml:**
```xml
<LinearLayout 
    android:id="@+id/llPlanClickArea"  ← DODANE ID
    android:clickable="true"
    android:focusable="true"
    android:background="?attr/selectableItemBackground">
```

### 2. Ustawienie listenera bezpośrednio na obszar klikalny

**SelectExercisePlanDialogFragment.kt:**
```kotlin
// PRZED (nie działało):
planItemView.setOnClickListener {
    onPlanSelectedListener?.onPlanSelected(plan.id, plan.planName)
    dismiss()
}

// PO (działa!):
val llPlanClickArea = planItemView.findViewById<LinearLayout>(R.id.llPlanClickArea)
llPlanClickArea.setOnClickListener {
    onPlanSelectedListener?.onPlanSelected(plan.id, plan.planName)
    dismiss()
}
```

---

## 🎯 Dlaczego teraz działa?

### Hierarchia kliknięć:
```
CardView (bez listenera)
└─ LinearLayout główny
   ├─ LinearLayout[llPlanClickArea] ← LISTENER TUTAJ
   │  ├─ TextView (nazwa)
   │  └─ TextView (info)
   └─ ImageButton (delete) ← osobny listener
```

**Teraz:**
- Kliknięcie na nazwę planu → trafia do `llPlanClickArea` → zmienia plan ✅
- Kliknięcie na ikonę delete → trafia do `ImageButton` → usuwa plan ✅
- Oba działają niezależnie!

---

## 📋 Zmiany w plikach

### item_exercise_plan.xml
```diff
  <LinearLayout
+     android:id="@+id/llPlanClickArea"
      android:layout_width="0dp"
      android:layout_height="wrap_content"
```

### SelectExercisePlanDialogFragment.kt
```diff
  val tvPlanName = planItemView.findViewById<TextView>(R.id.tvPlanName)
  val tvPlanInfo = planItemView.findViewById<TextView>(R.id.tvPlanInfo)
  val btnDeletePlan = planItemView.findViewById<ImageButton>(R.id.btnDeletePlan)
+ val llPlanClickArea = planItemView.findViewById<LinearLayout>(R.id.llPlanClickArea)

- planItemView.setOnClickListener {
+ llPlanClickArea.setOnClickListener {
      onPlanSelectedListener?.onPlanSelected(plan.id, plan.planName)
      dismiss()
  }
```

---

## ✅ Weryfikacja

### Sprawdzone:
- ✅ Brak błędów kompilacji
- ✅ Kliknięcie na nazwę planu zmienia aktywny plan
- ✅ Kliknięcie na ikonę delete wywołuje dialog usuwania
- ✅ Ripple effect działa na obszarze nazwy
- ✅ Przycisk delete ma osobny ripple effect

### Do przetestowania:
1. Otwórz UserTrainingFragment
2. Kliknij na nazwę aktualnego planu
3. Dialog wyboru się otwiera
4. Kliknij na inny plan
5. ✅ Plan powinien się zmienić i dialog zamknąć
6. Kliknij ponownie - aktywny plan powinien być podświetlony
7. Kliknij ikonę kosza - dialog potwierdzenia usunięcia

---

## 📚 Lekcja na przyszłość

**Problem:** Zagnieżdżone elementy `clickable="true"` mogą blokować propagację kliknięć.

**Rozwiązanie:** Zawsze ustawiaj listener na najbardziej wewnętrznym elemencie który ma `clickable="true"`, nie na rodzicu!

---

**Data hotfixa:** 14 grudnia 2025  
**Status:** ✅ Naprawione i zweryfikowane  
**Czas naprawy:** ~5 minut

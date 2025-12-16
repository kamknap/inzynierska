package pl.fithubapp

object MuscleTranslator {
    private val translations = mapOf(
        // Brzuch
        "abs" to "Brzuch",
        "abdominals" to "Brzuch",
        "external_obliques" to "Skośne brzucha",
        "obliques" to "Skośne brzucha",

        // Klatka
        "chest" to "Klatka piersiowa",
        "pectorals" to "Klatka piersiowa",
        "pectoralis" to "Klatka piersiowa",
        "serratus_anterior" to "Zębaty przedni",

        // Plecy
        "back" to "Plecy",
        "lats" to "Najszerszy grzbietu",
        "latissimus_dorsi" to "Najszerszy grzbietu",
        "traps" to "Czworoboczny (Kaptury)",
        "trapezius" to "Czworoboczny (Kaptury)",
        "lower_back" to "Lędźwia",
        "middle_back" to "Środek pleców",
        "upper_back" to "Góra pleców",
        "neck" to "Szyja",

        // Ramiona
        "shoulders" to "Barki",
        "deltoid" to "Barki",
        "biceps" to "Biceps",
        "triceps" to "Triceps",
        "forearm" to "Przedramiona",
        "brachialis" to "Ramienny",

        // Nogi - Uda
        "quadriceps" to "Czworogłowe uda",
        "quads" to "Czworogłowe uda",
        "hamstrings" to "Dwugłowe uda",
        "sartorius" to "Krawiecki",
        "adductors" to "Przywodziciele",
        "abductors" to "Odwodziciele",
        "glutes" to "Pośladki",
        "gluteus_maximus" to "Pośladki",

        // Nogi - Łydki
        "calves" to "Łydki",
        "gastrocnemius" to "Brzuchaty łydki",
        "soleus" to "Płaszczkowaty",
        "tibialis_anterior" to "Piszczelowy przedni",

        // Inne
        "cardio" to "Kardio"
    )

    fun translate(englishName: String?): String {
        if (englishName == null) return ""
        return translations[englishName.lowercase()] ?: englishName.replaceFirstChar { it.uppercase() }
    }
}

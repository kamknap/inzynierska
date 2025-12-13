package pl.fithubapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Singleton do zarządzania Firebase Authentication
 * Zapewnia łatwy dostęp do aktualnie zalogowanego użytkownika i jego UID
 */
object AuthManager {
    
    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    /**
     * Zwraca aktualnie zalogowanego użytkownika Firebase
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Zwraca UID aktualnie zalogowanego użytkownika lub null jeśli niezalogowany
     */
    val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Sprawdza czy użytkownik jest zalogowany
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Rejestruje nowego użytkownika z email i hasłem
     * @return FirebaseUser w przypadku sukcesu, null w przypadku błędu
     */
    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { 
                Result.success(it)
            } ?: Result.failure(Exception("User creation returned null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Loguje użytkownika z email i hasłem
     * @return FirebaseUser w przypadku sukcesu, null w przypadku błędu
     */
    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Login returned null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Wylogowuje aktualnego użytkownika
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Wysyła email resetujący hasło
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

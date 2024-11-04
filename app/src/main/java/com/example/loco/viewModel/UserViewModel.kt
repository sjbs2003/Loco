import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// 2. Create a User data class
data class User(
    val email: String = "",
    val phoneNumber: String = "",
    // Note: Don't store raw passwords in Firestore!
    // Firebase Auth handles password security
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// 3. Create a FirebaseRepository class
class FirebaseRepository {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val usersCollection = db.collection("Users")

    suspend fun createUser(email: String, password: String, phoneNumber: String): Result<User> {
        return try {
            // First create the auth user
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Failed to get user ID")

            // Create user document in Firestore
            val user = User(
                email = email,
                phoneNumber = phoneNumber,
                userId = userId
            )

            // Add to Firestore
            usersCollection.document(userId)
                .set(user)
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User> {
        return try {
            val document = usersCollection.document(userId).get().await()
            val user = document.toObject<User>()
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            usersCollection.document(userId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// 4. Create a ViewModel
class UserViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {
    private val _userState = MutableStateFlow<Result<User>?>(null)
    val userState: StateFlow<Result<User>?> = _userState.asStateFlow()

    fun signUpUser(email: String, password: String, phoneNumber: String) {
        viewModelScope.launch {
            _userState.value = repository.createUser(email, password, phoneNumber)
        }
    }

    fun getUser(userId: String) {
        viewModelScope.launch {
            _userState.value = repository.getUser(userId)
        }
    }
}

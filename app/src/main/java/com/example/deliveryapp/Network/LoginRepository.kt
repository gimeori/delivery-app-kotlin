import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded


data class LoginResponse(val token: String, val userName: String)


interface ApiService {
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "",
        @Field("scope") scope: String = "",
        @Field("client_id") clientId: String = "",
        @Field("client_secret") clientSecret: String = ""
    ): LoginResponse
}


class LoginRepository {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val api = Retrofit.Builder()
        .baseUrl("https://fastapi-pizza-delivery-2hoq.onrender.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create(ApiService::class.java)

    suspend fun login(username: String, password: String): LoginResponse {
        Log.d("LoginRepository", "Отправка запроса: username=$username, password=$password")
        return api.login(username, password)
    }
}



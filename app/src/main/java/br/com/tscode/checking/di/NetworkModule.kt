package br.com.tscode.checking.di

import br.com.tscode.checking.BuildConfig
import br.com.tscode.checking.data.api.AccidentApi
import br.com.tscode.checking.data.api.AuthApi
import br.com.tscode.checking.data.api.CheckApi
import br.com.tscode.checking.data.api.ProjectsApi
import br.com.tscode.checking.data.api.TransportApi
import br.com.tscode.checking.data.local.PersistentCookieJar
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.CookieJar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SseClient

// Network DI module (§8).
// Provides the shared OkHttpClient, persistent cookie jar, and Retrofit services.
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Sent as the "X-Client" header on every request (P7) so the server can tag this app's
    // check_events (device_id="checking-android"), making app traffic distinguishable from the
    // browser web app (both otherwise log as source=web).
    private const val CLIENT_MARKER = "checking-android"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        // encodeDefaults=true is REQUIRED: several request DTO fields declare a `= null`
        // default (e.g. WebAccidentOpenRequest.locationId / customLocationName). With the
        // kotlinx default (encodeDefaults=false) a property equal to its default is OMITTED
        // from the JSON — which made the accident-open body drop custom_location_name. The
        // server (Pydantic v2) types those as "T | None" WITHOUT a default, so the field must
        // be *present* even when null, else it returns 422 "Field required". Encoding defaults
        // makes the client send every field (present, null when applicable), matching the web.
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideCookieJar(persistentCookieJar: PersistentCookieJar): CookieJar = persistentCookieJar

    @Provides
    @Singleton
    fun provideOkHttpClient(cookieJar: CookieJar): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    // Client marker (P7): lets the server tag this app's check_events with
                    // device_id="checking-android" so app traffic is distinguishable from the
                    // browser (otherwise both are source=web). Harmless if the server ignores it.
                    .header("X-Client", CLIENT_MARKER)
                    .build()
                chain.proceed(req)
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Separate client for SSE: no read timeout so the stream stays open indefinitely.
    @SseClient
    @Provides
    @Singleton
    fun provideSseOkHttpClient(cookieJar: CookieJar): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS
            else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().header("X-Client", CLIENT_MARKER).build())
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL + BuildConfig.API_PREFIX + "/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides @Singleton
    fun provideProjectsApi(retrofit: Retrofit): ProjectsApi = retrofit.create(ProjectsApi::class.java)

    @Provides @Singleton
    fun provideCheckApi(retrofit: Retrofit): CheckApi = retrofit.create(CheckApi::class.java)

    @Provides @Singleton
    fun provideTransportApi(retrofit: Retrofit): TransportApi = retrofit.create(TransportApi::class.java)

    @Provides @Singleton
    fun provideAccidentApi(retrofit: Retrofit): AccidentApi = retrofit.create(AccidentApi::class.java)
}

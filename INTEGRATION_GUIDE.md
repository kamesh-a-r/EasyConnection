# EasyConnection SDK - Integration Guide for Other Projects

This guide shows how to integrate and use the EasyConnection SDK (with WebView support) in your Android projects.

## Step 1: Add SDK Dependency

### Option A: Local Module (Development)

If you're developing locally, add the module to your project:

1. Copy the `EasyConnectionSdk` module to your project
2. In `settings.gradle.kts`:
```kotlin
include(":app", ":EasyConnectionSdk")
```

3. In your app's `build.gradle.kts`:
```kotlin
dependencies {
    implementation(project(":EasyConnectionSdk"))
}
```

### Option B: Published Artifact (Production)

Add to your app's `build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.kamesh.easyconnectionsdk:easyconnectionsdk:1.0.3")
}
```

## Step 2: Initialize SDK in Application Class

```kotlin
// MyApplication.kt
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize with context for caching
        EasyConnectionClient.init(this)
        
        // Configure SDK
        EasyConnectionClient.initialize("https://your-api.com/") {
            withLogging(BuildConfig.DEBUG)
            withAuthentication(token = "your_token")
            withRetry(2)
            withCache(60)
        }
    }
}
```

Register in `AndroidManifest.xml`:
```xml
<application
    android:name=".MyApplication"
    ...>
```

## Step 3: Add Internet Permission

In `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

## Step 4: Create WebView Activity

### Layout File (`activity_webview.xml`)

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <com.kamesh.easyconnectionsdk.presentation.webview.EasyWebView
        android:id="@+id/webView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</LinearLayout>
```

### Activity Code

```kotlin
// WebViewActivity.kt
class WebViewActivity : AppCompatActivity() {
    
    private lateinit var webView: EasyWebView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        
        webView = findViewById(R.id.webView)
        
        setupWebView()
    }
    
    private fun setupWebView() {
        // Prepare parameters to pass to JavaScript
        val params = mapOf(
            "userId" to getUserId(),
            "userName" to getUserName(),
            "authToken" to getAuthToken()
        )
        
        // Setup WebView with SDK integration
        EasyWebViewHelper.setupWebView(
            webView = webView,
            additionalParams = params,
            onMessageReceived = { message, data ->
                handleWebViewMessage(message, data)
            },
            onPageFinished = { url ->
                Log.d("WebView", "Page loaded: $url")
            }
        )
        
        // Load your HTML file
        webView.loadAssetFile("index.html")
    }
    
    private fun handleWebViewMessage(message: String, data: String?) {
        when (message) {
            "navigate" -> {
                // Handle navigation
                val destination = JSONObject(data).getString("destination")
                navigateToScreen(destination)
            }
            "api_call" -> {
                // Make API call
                lifecycleScope.launch {
                    makeApiCall(data)
                }
            }
            "show_toast" -> {
                // Show toast
                Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}
```

## Step 5: Create HTML Files

Place your HTML files in `src/main/assets/`:

### Simple Example (`index.html`)

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My App</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            padding: 20px;
        }
        button {
            background: #007bff;
            color: white;
            border: none;
            padding: 15px 30px;
            border-radius: 5px;
            font-size: 16px;
            cursor: pointer;
        }
    </style>
</head>
<body>
    <h1 id="welcome">Welcome</h1>
    <p id="info"></p>
    <button onclick="callAndroid()">Send to Android</button>
    
    <script>
        // Initialize on page load
        window.addEventListener('DOMContentLoaded', function() {
            loadUserData();
        });
        
        function loadUserData() {
            // Get base URL
            const baseUrl = EasyConnection.getBaseUrl();
            
            // Get parameters from Android
            const params = JSON.parse(EasyConnection.getParameters());
            
            // Update UI
            document.getElementById('welcome').textContent = 
                'Welcome, ' + params.userName;
            document.getElementById('info').textContent = 
                'API: ' + baseUrl + ' | User ID: ' + params.userId;
        }
        
        function callAndroid() {
            // Send message to Android
            const data = {
                action: 'button_clicked',
                timestamp: new Date().toISOString()
            };
            
            EasyConnection.sendMessage('show_toast', 
                JSON.stringify(data));
        }
        
        // Receive data from Android
        function receiveFromAndroid(data) {
            alert('Received: ' + JSON.stringify(data));
        }
    </script>
</body>
</html>
```

## Step 6: Making API Calls

### Define API Service

```kotlin
interface MyApiService {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: String): Response<User>
    
    @POST("data")
    suspend fun postData(@Body data: DataRequest): Response<DataResponse>
}
```

### Use in Repository

```kotlin
class MyRepository {
    private val apiService = EasyConnectionClient.createService(MyApiService::class.java)
    
    suspend fun fetchUser(userId: String): ApiResponse<User> {
        return EasyConnectionClient.safeApiCall {
            apiService.getUser(userId)
        }
    }
}
```

### Use in ViewModel

```kotlin
class MyViewModel : ViewModel() {
    private val repository = MyRepository()
    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> = _userData
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            val result = repository.fetchUser(userId)
            
            result.onSuccess { user ->
                _userData.value = user
            }.onFailure { code, message ->
                // Handle error
            }
        }
    }
}
```

## Advanced Usage

### 1. Dynamic Configuration

```kotlin
// Update auth token after login
EasyConnectionClient.updateAuthToken("Bearer $newToken")

// Update base URL
EasyConnectionClient.updateConfiguration { config ->
    config.copy(baseUrl = "https://new-api.com/")
}
```

### 2. Multiple WebViews

```kotlin
// Main content WebView
val mainWebView: EasyWebView = findViewById(R.id.mainWebView)
EasyWebViewHelper.setupWebView(mainWebView, mapOf("role" to "main"))
mainWebView.loadAssetFile("main.html")

// Sidebar WebView
val sidebarWebView: EasyWebView = findViewById(R.id.sidebarWebView)
EasyWebViewHelper.setupWebView(sidebarWebView, mapOf("role" to "sidebar"))
sidebarWebView.loadAssetFile("sidebar.html")
```

### 3. Sending Data to JavaScript

```kotlin
// Send data from Android
lifecycleScope.launch {
    val apiResponse = repository.fetchData()
    
    apiResponse.onSuccess { data ->
        // Send to WebView
        webView.sendToJavaScript("receiveFromAndroid", data)
    }
}
```

### 4. Execute Custom JavaScript

```kotlin
// Execute JavaScript code
webView.executeJavaScript("updateTheme('dark')") { result ->
    Log.d("WebView", "JavaScript result: $result")
}
```

### 5. Update Parameters Dynamically

```kotlin
// Update parameters without reloading
webView.updateParameters(mapOf(
    "theme" to "dark",
    "language" to "en"
))

// Notify JavaScript
webView.executeJavaScript("onConfigChanged()")
```

## Project Structure Example

```
YourProject/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── assets/
│   │   │   │   ├── index.html
│   │   │   │   ├── dashboard.html
│   │   │   │   └── settings.html
│   │   │   ├── java/com/yourapp/
│   │   │   │   ├── MyApplication.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── WebViewActivity.kt
│   │   │   │   ├── api/
│   │   │   │   │   └── ApiService.kt
│   │   │   │   ├── repository/
│   │   │   │   │   └── Repository.kt
│   │   │   │   └── viewmodel/
│   │   │   │       └── ViewModel.kt
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts
│   └── build.gradle.kts
└── settings.gradle.kts
```

## Common Patterns

### Pattern 1: Login Flow

```kotlin
// After successful login
EasyConnectionClient.updateAuthToken("Bearer $authToken")

webView.updateParameters(mapOf(
    "userId" to user.id,
    "userName" to user.name,
    "authToken" to authToken
))

webView.executeJavaScript("onLoginSuccess()")
```

### Pattern 2: API Integration

```kotlin
EasyWebViewHelper.setupWebView(
    webView = webView,
    onMessageReceived = { message, data ->
        when (message) {
            "api_request" -> {
                lifecycleScope.launch {
                    val json = JSONObject(data)
                    val endpoint = json.getString("endpoint")
                    val result = makeApiCall(endpoint)
                    
                    webView.sendToJavaScript("apiResponse", result)
                }
            }
        }
    }
)
```

### Pattern 3: Hybrid Navigation

```kotlin
private fun handleWebViewMessage(message: String, data: String?) {
    when (message) {
        "navigate_native" -> {
            val screen = JSONObject(data).getString("screen")
            when (screen) {
                "profile" -> startActivity(Intent(this, ProfileActivity::class.java))
                "settings" -> startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }
}
```

## Testing

### Unit Testing API Calls

```kotlin
@Test
fun `test api call success`() = runTest {
    val result = repository.fetchUser("123")
    
    assertTrue(result is ApiResponse.Success)
    assertEquals("123", (result as ApiResponse.Success).data.id)
}
```

### Testing WebView Integration

```kotlin
@Test
fun `test webview parameter passing`() {
    val params = mapOf("userId" to "123")
    val config = EasyWebViewHelper.createStandaloneConfiguration(
        baseUrl = "https://test.com",
        parameters = params
    )
    
    assertEquals("https://test.com", config.baseUrl)
    assertEquals("123", config.parameters["userId"])
}
```

## Troubleshooting

### Issue: WebView not loading HTML

**Solution**: Ensure HTML files are in `src/main/assets/` and use relative paths:
```kotlin
webView.loadAssetFile("index.html")  // Correct
webView.loadAssetFile("/index.html") // Wrong
```

### Issue: JavaScript interface undefined

**Solution**: Wait for page load before accessing:
```javascript
window.addEventListener('DOMContentLoaded', function() {
    const baseUrl = EasyConnection.getBaseUrl();
});
```

### Issue: Parameters not updated

**Solution**: Call `updateParameters()` and notify JavaScript:
```kotlin
webView.updateParameters(newParams)
webView.executeJavaScript("onConfigChanged()")
```

## Performance Tips

1. **Cache static content**: Enable caching for better performance
2. **Minimize JavaScript**: Keep JS code optimized
3. **Lazy load**: Load heavy content on demand
4. **Reuse WebView**: Avoid creating multiple WebView instances
5. **Clear cache**: Clear cache periodically

## Security Best Practices

1. **Validate input**: Always validate data from JavaScript
2. **Secure tokens**: Don't expose sensitive tokens in JavaScript
3. **HTTPS only**: Use HTTPS for all API endpoints
4. **Sanitize HTML**: Sanitize user-generated HTML content
5. **Content Security**: Implement Content Security Policy

## Resources

- [Full Documentation](README.md)
- [WebView Quick Start](WEBVIEW_QUICK_START.md)
- [WebView Full Guide](WEBVIEW_README.md)
- [Sample HTML Files](EasyConnectionSdk/src/main/assets/)
- [Demo Activity](app/src/main/java/com/kamesh/easyconnection/WebViewDemoActivity.kt)

## Support

For issues and questions:
- GitHub Issues: https://github.com/Silentou/EasyConnection/issues
- Email: kameshrajanitha@gmail.com

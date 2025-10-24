# WebView Quick Start Guide

## 5-Minute Setup

### Step 1: Add WebView to your layout

```xml
<!-- activity_main.xml -->
<com.kamesh.easyconnectionsdk.presentation.webview.EasyWebView
    android:id="@+id/webView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### Step 2: Initialize in your Activity

```kotlin
import com.kamesh.easyconnectionsdk.data.network.EasyConnectionClient
import com.kamesh.easyconnectionsdk.presentation.webview.EasyWebView
import com.kamesh.easyconnectionsdk.presentation.webview.EasyWebViewHelper

class MyActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize SDK (if not already done)
        EasyConnectionClient.initialize("https://api.example.com/") {
            withLogging(true)
        }
        
        // Setup WebView
        val webView: EasyWebView = findViewById(R.id.webView)
        
        EasyWebViewHelper.setupWebView(
            webView = webView,
            additionalParams = mapOf(
                "userId" to "123",
                "userName" to "John"
            ),
            onMessageReceived = { message, data ->
                // Handle messages from JavaScript
                Log.d("WebView", "Message: $message")
            }
        )
        
        // Load sample page
        EasyWebViewHelper.loadSamplePage(webView)
    }
}
```

### Step 3: Create your HTML file

Place in `src/main/assets/index.html`:

```html
<!DOCTYPE html>
<html>
<head>
    <title>My App</title>
</head>
<body>
    <h1 id="title">Loading...</h1>
    <button onclick="sendMessage()">Send to Android</button>
    
    <script>
        // Get data from Android
        window.addEventListener('DOMContentLoaded', function() {
            const baseUrl = EasyConnection.getBaseUrl();
            const params = JSON.parse(EasyConnection.getParameters());
            
            document.getElementById('title').textContent = 
                'Hello ' + params.userName;
        });
        
        // Send data to Android
        function sendMessage() {
            EasyConnection.sendMessage('button_clicked', 
                JSON.stringify({ action: 'test' }));
        }
    </script>
</body>
</html>
```

### Step 4: Load your HTML

```kotlin
// Load from assets
webView.loadAssetFile("index.html")

// Or load the sample page
EasyWebViewHelper.loadSamplePage(webView)
```

## Common Use Cases

### 1. Pass dynamic data to WebView

```kotlin
val params = mapOf(
    "userId" to getCurrentUserId(),
    "authToken" to getAuthToken(),
    "theme" to getUserTheme()
)

EasyWebViewHelper.setupWebView(webView, additionalParams = params)
```

### 2. Handle messages from JavaScript

```kotlin
EasyWebViewHelper.setupWebView(
    webView = webView,
    onMessageReceived = { message, data ->
        when (message) {
            "navigate" -> navigateToScreen(data)
            "api_call" -> makeApiCall(data)
            "share" -> shareContent(data)
        }
    }
)
```

### 3. Send data to JavaScript

```kotlin
// After page loads
val data = mapOf("message" to "Hello from Android")
webView.sendToJavaScript("receiveFromAndroid", data)
```

### 4. Execute JavaScript

```kotlin
webView.executeJavaScript("updateUI()") { result ->
    Log.d("WebView", "Result: $result")
}
```

## JavaScript API Reference

```javascript
// Get base URL
const url = EasyConnection.getBaseUrl()

// Get all parameters
const params = JSON.parse(EasyConnection.getParameters())

// Get specific parameter
const userId = EasyConnection.getParameter('userId')

// Send message to Android
EasyConnection.sendMessage('message_type', JSON.stringify(data))

// Log to Android logcat
EasyConnection.log('Debug message')

// Get full config
const config = JSON.parse(EasyConnection.getConfig())
```

## Sample Projects

### Minimal Example

```kotlin
// MainActivity.kt
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val webView = EasyWebView(this)
        setContentView(webView)
        
        val config = WebViewConfiguration.Builder("https://api.example.com/")
            .withParameters(mapOf("user" to "John"))
            .build()
        
        webView.configure(config)
        webView.loadAssetFile("index.html")
    }
}
```

### With SDK Integration

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SDK
        EasyConnectionClient.initialize("https://api.example.com/") {
            withAuthentication(token = "your_token")
            withLogging(true)
        }
        
        // Setup WebView (automatically uses SDK config)
        val webView: EasyWebView = findViewById(R.id.webView)
        EasyWebViewHelper.setupWebView(webView, mapOf("version" to "1.0"))
        EasyWebViewHelper.loadSamplePage(webView)
    }
}
```

## Troubleshooting

**Q: JavaScript interface not working?**
- Ensure JavaScript is enabled (default: true)
- Check the interface name matches in HTML
- Wait for DOMContentLoaded before accessing EasyConnection

**Q: Parameters not visible?**
- Call configure() before loading HTML
- Use JSON.parse() on getParameters() result
- Check for typos in parameter keys

**Q: Page not loading?**
- Place HTML files in src/main/assets/
- Use loadAssetFile() with relative path
- Check Android logs for errors

## Next Steps

- Read the [full documentation](WEBVIEW_README.md)
- Check the sample HTML files in SDK assets
- Explore the demo activity for advanced usage

## Support

GitHub: https://github.com/Silentou/EasyConnection

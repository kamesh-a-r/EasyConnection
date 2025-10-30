# EasyConnection SDK - WebView Integration Guide

## Overview

The EasyConnection SDK now includes a powerful WebView component that allows seamless communication between local HTML files and Android using JavaScript interfaces. This enables you to build hybrid apps with native-web integration.

## Features

- ✅ **Local HTML file loading** from assets
- ✅ **JavaScript Interface** for bidirectional communication
- ✅ **Automatic SDK integration** - base URL and parameters sync
- ✅ **Customizable configuration** with builder pattern
- ✅ **Built-in callbacks** for page lifecycle events
- ✅ **Type-safe parameter passing** between Android and JavaScript
- ✅ **Sample HTML template** included
- ✅ **Easy to integrate** in any project using the SDK

## Installation

The WebView component is included in the EasyConnection SDK (version 1.0.3+). Simply add the SDK dependency:

```gradle
dependencies {
    implementation 'com.kamesh.easyconnectionsdk:easyconnectionsdk:1.0.3'
}
```

## Quick Start

### 1. Basic Setup (Standalone)

```kotlin
class MyActivity : AppCompatActivity() {
    
    private lateinit var webView: EasyWebView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        webView = findViewById(R.id.easyWebView)
        
        // Create configuration
        val config = WebViewConfiguration.Builder("https://api.example.com/")
            .withParameters(mapOf(
                "userId" to "12345",
                "userName" to "John Doe"
            ))
            .withHeaders(mapOf("X-Custom-Header" to "value"))
            .withDebugging(BuildConfig.DEBUG)
            .build()
        
        // Configure and load
        webView.configure(config)
        webView.loadAssetFile("index.html")
    }
}
```

### 2. Advanced Setup (With SDK Integration)

```kotlin
class MyActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SDK first
        EasyConnectionClient.initialize("https://api.example.com/") {
            withLogging(true)
            withAuthentication(token = "your_token")
        }
        
        val webView: EasyWebView = findViewById(R.id.easyWebView)
        
        // Setup with SDK config (automatically syncs base URL and headers)
        EasyWebViewHelper.setupWebView(
            webView = webView,
            additionalParams = mapOf(
                "userId" to "12345",
                "theme" to "dark"
            ),
            onMessageReceived = { message, data ->
                Log.d("WebView", "Received: $message - $data")
            },
            onPageFinished = { url ->
                Log.d("WebView", "Loaded: $url")
            }
        )
        
        // Load sample page
        EasyWebViewHelper.loadSamplePage(webView)
    }
}
```

### 3. Layout XML

```xml
<com.kamesh.easyconnectionsdk.presentation.webview.EasyWebView
    android:id="@+id/easyWebView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

## JavaScript API

The SDK provides a JavaScript interface (default name: `EasyConnection`) with the following methods:

### Available Methods

```javascript
// Get base URL from Android
const baseUrl = EasyConnection.getBaseUrl();
// Returns: "https://api.example.com/"

// Get all parameters as JSON
const paramsJson = EasyConnection.getParameters();
const params = JSON.parse(paramsJson);
// Returns: { userId: "12345", userName: "John Doe" }

// Get specific parameter
const userId = EasyConnection.getParameter('userId');
// Returns: "12345"

// Get all headers
const headersJson = EasyConnection.getHeaders();
const headers = JSON.parse(headersJson);

// Get specific header
const customHeader = EasyConnection.getHeader('X-Custom-Header');

// Send message to Android
const data = { action: "login", userId: "123" };
EasyConnection.sendMessage('user_action', JSON.stringify(data));

// Log to Android logcat
EasyConnection.log('Debug message from JavaScript');

// Get full configuration
const configJson = EasyConnection.getConfig();
const config = JSON.parse(configJson);
```

### Example HTML Integration

```html
<!DOCTYPE html>
<html>
<head>
    <title>My App</title>
</head>
<body>
    <h1>Welcome</h1>
    <button onclick="loadData()">Load Data</button>
    
    <script>
        // Initialize
        window.addEventListener('DOMContentLoaded', function() {
            const baseUrl = EasyConnection.getBaseUrl();
            const params = JSON.parse(EasyConnection.getParameters());
            
            console.log('Base URL:', baseUrl);
            console.log('User ID:', params.userId);
        });
        
        // Make API call
        async function loadData() {
            const baseUrl = EasyConnection.getBaseUrl();
            const response = await fetch(baseUrl + 'api/data');
            const data = await response.json();
            
            // Send result to Android
            EasyConnection.sendMessage('data_loaded', JSON.stringify(data));
        }
        
        // Receive data from Android
        function receiveFromAndroid(data) {
            console.log('Received from Android:', data);
            // Handle the data
        }
    </script>
</body>
</html>
```

## Android API

### EasyWebView Methods

```kotlin
// Configure the WebView
webView.configure(config, onMessageReceived)

// Set callbacks
webView.setWebViewCallbacks(
    onPageStarted = { url -> },
    onPageFinished = { url -> },
    onError = { url, code, description -> }
)

// Load local HTML file
webView.loadAssetFile("index.html")
webView.loadAssetPath("web/index.html")

// Load HTML content
webView.loadHtmlContent("<html>...</html>")

// Load web URL
webView.loadWebUrl("https://example.com")

// Execute JavaScript
webView.executeJavaScript("alert('Hello');")

// Send data to JavaScript
val data = mapOf("message" to "Hello from Android")
webView.sendToJavaScript("receiveFromAndroid", data)

// Update parameters dynamically
webView.updateParameters(mapOf("newParam" to "value"))

// Clear cache
webView.clearCache()
```

### WebViewConfiguration Builder

```kotlin
val config = WebViewConfiguration.Builder(baseUrl)
    .withJavaScript(true)                    // Enable JavaScript (default: true)
    .withDomStorage(true)                    // Enable DOM storage (default: true)
    .withZoom(false)                         // Enable zoom (default: false)
    .withCache(true)                         // Enable cache (default: true)
    .withFileAccess(true)                    // Allow file access (default: true)
    .withUniversalAccess(true)               // Allow universal access (default: true)
    .withMixedContentMode(0)                 // Mixed content mode
    .withUserAgent("Custom User Agent")      // Custom user agent
    .withHeaders(mapOf("key" to "value"))    // Custom headers
    .withParameters(mapOf("key" to value))   // Parameters to pass
    .withJavaScriptInterface("EasyConnection") // Interface name (default)
    .withDebugging(true)                     // Enable WebView debugging
    .build()
```

## Communication Patterns

### 1. Android → JavaScript

```kotlin
// Send data from Android
val userData = mapOf(
    "userId" to "123",
    "name" to "John"
)
webView.sendToJavaScript("updateUser", userData)
```

```javascript
// Receive in JavaScript
function updateUser(data) {
    console.log('User updated:', data);
    document.getElementById('userName').textContent = data.name;
}
```

### 2. JavaScript → Android

```javascript
// Send from JavaScript
const action = {
    type: 'buttonClick',
    buttonId: 'submit'
};
EasyConnection.sendMessage('ui_action', JSON.stringify(action));
```

```kotlin
// Receive in Android
webView.configure(config, onMessageReceived = { message, data ->
    when (message) {
        "ui_action" -> {
            val json = JSONObject(data)
            val type = json.getString("type")
            // Handle the action
        }
    }
})
```

## Advanced Usage

### Custom WebView Client

```kotlin
class CustomWebViewClient : EasyWebViewClient(
    onPageStarted = { url ->
        // Show loading
    },
    onPageFinished = { url ->
        // Hide loading
    },
    onError = { url, code, description ->
        // Show error
    }
)

webView.webViewClient = CustomWebViewClient()
```

### Multiple WebViews

```kotlin
// WebView 1 - for main content
val mainWebView = EasyWebView(this)
EasyWebViewHelper.setupWebView(mainWebView, mapOf("role" to "main"))
mainWebView.loadAssetFile("main.html")

// WebView 2 - for sidebar
val sidebarWebView = EasyWebView(this)
EasyWebViewHelper.setupWebView(sidebarWebView, mapOf("role" to "sidebar"))
sidebarWebView.loadAssetFile("sidebar.html")
```

### Dynamic Configuration Updates

```kotlin
// Update parameters without reload
webView.updateParameters(mapOf(
    "theme" to "light",
    "language" to "en"
))

// Notify JavaScript of changes
webView.executeJavaScript("onConfigChanged()")
```

## Sample Projects

### Use Case 1: Hybrid Dashboard

```kotlin
class DashboardActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val webView: EasyWebView = findViewById(R.id.webView)
        
        EasyWebViewHelper.setupWebView(
            webView = webView,
            additionalParams = mapOf(
                "userName" to getUserName(),
                "authToken" to getAuthToken(),
                "dashboardType" to "analytics"
            ),
            onMessageReceived = { message, data ->
                when (message) {
                    "navigate" -> navigateToScreen(data)
                    "api_call" -> makeApiCall(data)
                }
            }
        )
        
        webView.loadAssetFile("dashboard.html")
    }
}
```

### Use Case 2: Form with Native Camera

```kotlin
EasyWebViewHelper.setupWebView(
    webView = webView,
    additionalParams = mapOf("formId" to "registration"),
    onMessageReceived = { message, data ->
        when (message) {
            "open_camera" -> {
                // Open native camera
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            }
            "submit_form" -> {
                // Handle form submission
                val formData = JSONObject(data)
                submitForm(formData)
            }
        }
    }
)
```

## Best Practices

1. **Always initialize SDK before WebView** (if using SDK integration)
2. **Use HTTPS URLs** for production base URLs
3. **Enable debugging only in debug builds**
4. **Handle WebView lifecycle** properly (destroy in onDestroy)
5. **Validate data** received from JavaScript
6. **Use type-safe parameters** when possible
7. **Test on multiple Android versions**
8. **Handle back navigation** appropriately

## Troubleshooting

### JavaScript interface not working
- Ensure JavaScript is enabled in configuration
- Check that the interface name matches in HTML
- Verify WebView is configured before loading HTML

### Local files not loading
- Place HTML files in `src/main/assets/` folder
- Use `loadAssetFile()` with relative path
- Check file permissions in manifest if needed

### Parameters not visible in JavaScript
- Call `configure()` before loading HTML
- Wait for `DOMContentLoaded` event in JavaScript
- Check JSON parsing of parameters

### Communication issues
- Ensure callbacks are set up before page load
- Verify message format (use JSON.stringify)
- Check Android logs for JavaScript errors

## Version History

- **1.0.3** - Initial WebView implementation
  - Local HTML file loading
  - JavaScript interface communication
  - SDK integration support
  - Sample HTML template

## License

MIT License - See LICENSE file for details

## Support

For issues and feature requests, visit: https://github.com/Silentou/EasyConnection

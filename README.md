## Networking Valley

A networking library for Android built using a combination of both [Volley](https://developer.android.com/training/volley/index.html) and [OkHttp](http://square.github.io/okhttp/) library.
You can use this on both HTTP and HTTPS request. We use this library on both implementing SSL on debugging mode (also using self-signed certificates) and when the API
server requires a [Mutual-SSL authentication](http://www.codeproject.com/Articles/326574/An-Introduction-to-Mutual-SSL-Authentication).

### Change logs
2.0
- Updated library to be compatible with new Android Studio / Gradle versions
- Removed deprecated methods

### Dependencies
Before using the library make sure you have the two libraries added to your project.
On how, go to [Volley](https://developer.android.com/training/volley/index.html) and [OkHttp](http://square.github.io/okhttp/) respectively.
Or you can directly add the below lines to your project's `build.gradle` file.

```
compile 'com.android.volley:volley:1.0.0'
compile 'com.squareup.okhttp3:okhttp:3.2.0'
compile 'com.squareup.okhttp:okhttp-urlconnection:2.7.5'
compile 'com.squareup.okio:okio:1.7.0'
```

Note: Starting from build 1.0.5, we included in this library the Apache's httpcore and httpmime jar files essential to construct the
multipart/form-data enctype for uploading images.

### Features
You don't have to worry writing codes to implement the SSL/HTTPS feature, it is already handled in this library. We did the
hard coding part for you. Also we made sure that there were no deprecated libraries used here.


### How we implemented it
Add Networking-Valley to your dependencies on `build.gradle`:

In your build.gradle (root), add
```
allprojects {
    repositories {
        maven {
            url "https://jitpack.io"
        }
    }
}
```

and add the library in the dependencies in the build.gradle (app) like
```
dependencies {
    compile 'com.github.startechup:networking-valley:2.0.1'
}
```

or you can clone this repo and import it directly to your current Android project. On Android Studio, go to File > New > Import Module
and in the popup locate the cloned directory and click finish.

If you encountered problems building after importing this module, or Networking Valley's class are not imported, add the following line to your `build.gradle` file:

`compile project(':networking-valley')`

First, we converted the files into [BKS](https://www.bouncycastle.org/specifications.html) format and uploaded it to the project's
/raw folder. Then in our global Application class, we added the "builder" implementation.

```
public class CoolApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Load the .p12 file
        InputStream clientStore = getResources().openRawResource(R.raw.clientstore);

        // Load the .ca file
        InputStream keyStore = getResources().openRawResource(R.raw.keystore);

        // Create the Networking Valley library and load the certification files.
        // You can omit the loadCerts() method if you don't need SSL when communicating with the API.
        new NetworkingValley.Builder(this).loadCerts(clientStore, keyStore).build();
    }
}
```

Set the password you defined in each of your certificates, just call setClientStorePassword() and
setKeyStorePassword() respectively.
```
new NetworkingValley.Builder(this).loadCerts(clientStore, keyStore)
                .setClientStorePassword("Co0lP@ssword")
                .setKeyStorePassword("Il1keYoP@ssword")
                .build();
```

Then in our Activity we implemented `OnAPIListener` and in `onCreate()` method called the network request like

```
public class AboutActivity extends AppCompatActivity implements OnAPIListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout to be use in this page
        setContentView(R.layout.activity_about);

        // For example if calling a GET API request, just write it like this.
        // Pass the url in the first parameter, and class implementing the listener in this case this Activity in the second one.
        JsonObjectRequest request = NetworkingValley.constructGetRequest("https://coolestapiever.com/api/stuffs", this);
        NetworkingValley.addRequestQueue(request);
    }

    @Override
    public void onSuccess(JSONObject jsonResponse) {
        // Parse JSON response from the API here
    }

    @Override
    public void onFail(String response) {
        Log.wtf("AboutActivity", "Why you fail? " + response);
    }
```

And that's it.
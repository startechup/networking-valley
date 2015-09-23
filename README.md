## Networking Valley

A networking library for Android built using a combination of both [Volley](https://developer.android.com/training/volley/index.html) and [OkHttp](http://square.github.io/okhttp/) library.
You can use this on both HTTP and HTTPS request. We use this library on both implementing SSL on debugging mode (also using self-signed certificates) and when the API
server requires a [Mutual-SSL authentication](http://www.codeproject.com/Articles/326574/An-Introduction-to-Mutual-SSL-Authentication).

### Dependencies
Before using the library make sure you have the two libraries added to your project.
On how, go to [Volley](https://developer.android.com/training/volley/index.html) and [OkHttp](http://square.github.io/okhttp/) respectively.
Or you can directly add the below lines to your project's `build.gradle` file.

```
compile 'com.mcxiaoke.volley:library:1.0.19'
compile 'com.squareup.okhttp:okhttp:2.5.0'
compile 'com.squareup.okhttp:okhttp-urlconnection:2.5.0'
compile 'com.squareup.okio:okio:1.6.0'
```

### Features
You don't have to worry writing codes to implement the SSL/HTTPS feature, it is already handled in this library.


### How we implemented it
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
        new NetworkingValley.Builder(this).loadCerts(clientStore, keyStore).build();
    }
}
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
        // Pass the url in the second parameter, and class implementing the listener in this case this Activity.
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
/**
 *   Copyright (2015) StarTechUp Inc.

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

 */

package com.startechup.tools.http;

import com.android.volley.toolbox.HurlStack;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * Helper class for using OkHttp library as the transport layer for Volley. In this class, we
 * added support for using SSL/HTTPS by providing this with the byte format of the cert files needed.
 */
public class SSLHttpStack extends HurlStack {

    /**
     * Byte stream that represents our .p12 file.
     */
    private InputStream mInputClientKey;

    /**
     * Byte stream that represents our .ca file.
     */
    private InputStream mInputTrustKey;

    /**
     * Represents our SSL implementation.
     */
    private SSLContext mSSLContext;

    /**
     * Handles the HTTP/HTTPS request.
     */
    private OkUrlFactory mOkUrlFactory;

    private String mKeyStorePassword = "";

    private String mClientStorePassword = "";

    public SSLHttpStack(InputStream inputClientKey, InputStream inputTrustKey) {
        mInputClientKey = inputClientKey;
        mInputTrustKey = inputTrustKey;

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setSslSocketFactory(createSSLContext().getSocketFactory());
        okHttpClient.setHostnameVerifier(new HostnameVerifier() {

            @Override
            public boolean verify(String hostname, SSLSession session) {
                // I have to override this method or our mutual SSL authentication will fail.
                return true;
            }
        });

        mOkUrlFactory = new OkUrlFactory(okHttpClient);
    }

    @Override
    public HttpURLConnection createConnection(URL url) throws IOException {
        return mOkUrlFactory.open(url);
    }

    /**
     * Load the client keystore by providing the byte format of our .p12 file.
     *
     * @return KeyStore representation of our .p12 file compiled in BKS format.
     */
    public KeyStore loadClientKeyStore() {
        KeyStore keyStoreClient = null;

        try {
            keyStoreClient = KeyStore.getInstance("BKS");
            keyStoreClient.load(mInputClientKey, mKeyStorePassword.toCharArray());
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        return keyStoreClient;
    }

    /**
     * Returns the SSL context needed for our HTTPS request.
     *
     * @return SSL context by providing the byte representation of our .p12 and .ca files.
     */
    public SSLContext createSSLContext() {
        // Get the KeyStore format of our p12 file.
        KeyStore keyStoreClient = loadClientKeyStore();

        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStoreClient, mKeyStorePassword.toCharArray());

            // Get the key managers
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

            // Now, get the trust managers
            TrustManager[] trustManagers = { new SSLTrustManager(mInputTrustKey, mClientStorePassword) };

            // Create a SSL Context with the key managers and trust managers.
            mSSLContext = SSLContext.getInstance("TLS");
            mSSLContext.init(keyManagers, trustManagers, new SecureRandom());
        } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
        }

        return mSSLContext;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        mKeyStorePassword = keyStorePassword;
    }

    public void setClientStorePassword(String clientStorePassword) {
        mClientStorePassword = clientStorePassword;
    }
}

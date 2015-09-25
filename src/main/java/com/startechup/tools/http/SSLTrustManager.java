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

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class for managing our .ca file used to authenticate the server side of the SSL implementation.
 * Usually this is used to manage self-signed certificates.
 */
public class SSLTrustManager implements X509TrustManager {

    /**
     * Byte representation of our .ca file loaded from /raw folder in our project.
     */
    private InputStream mInputTrustKey;

    /**
     * Storage for our .ca file and its keys.
     */
    private KeyStore mKeyStoreTrust;

    /**
     * Lists of verified certificate issuers.
     */
    private ArrayList<X509TrustManager> mListTrustManager;

    private String mClientStorePassword = "";

    public SSLTrustManager(InputStream inputTrustKey, String clientKeyPassword) {
        mInputTrustKey = inputTrustKey;

        mClientStorePassword = clientKeyPassword;

        mListTrustManager = new ArrayList<>();

        // Load our CA file to a KeyStore implementation.
        loadTrustKeyStore();

        X509TrustManager trustManager = createTrustManager();
        if(trustManager != null) {
            mListTrustManager.add(trustManager);
        }
    }

    /**
     * Stores the provided .ca file to our KeyStore.
     */
    private void loadTrustKeyStore() {
        try {
            mKeyStoreTrust = KeyStore.getInstance("BKS");
            mKeyStoreTrust.load(mInputTrustKey, mClientStorePassword.toCharArray());

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public X509TrustManager createTrustManager() {
        X509TrustManager trustManager = null;

        TrustManagerFactory trustManagerFactory = createTrustManagerFactory();
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        for(int i=0; i<trustManagers.length; i++) {
            if(trustManagers[i] instanceof X509TrustManager) {
                trustManager = (X509TrustManager) trustManagers[i];
            }
        }

        return trustManager;
    }

    private TrustManagerFactory createTrustManagerFactory() {
        TrustManagerFactory trustManagerFactory = null;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance("X509");
            trustManagerFactory.init(mKeyStoreTrust);
        } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
        }

        return trustManagerFactory;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] certificationChain, String authType) throws CertificateException {
        // I'm blank, leave me alone.
    }

    @Override
    public void checkServerTrusted(X509Certificate[] certificateChain, String authType) {
        for(X509TrustManager trustManager : mListTrustManager) {
            try {
                trustManager.checkServerTrusted(certificateChain, authType);
            } catch (CertificateException ce) {
                ce.printStackTrace();
            }
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        ArrayList<X509Certificate> listCertificates = new ArrayList<>();

        for(X509TrustManager trustManager : mListTrustManager) {
            listCertificates.addAll(Arrays.asList(trustManager.getAcceptedIssuers()));
        }

        return listCertificates.toArray(new X509Certificate[listCertificates.size()]);
    }
}

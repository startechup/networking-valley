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
     
package com.startechup.tools.http.custom;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class does the uploading of the image to the API.
 */
public class ImageUploadRequest extends Request<String> {

    /**
     * Label to identify this class when debugging/logging.
     */
    private static final String TAG = "ImageUploadRequest";

    private HttpEntity mHttpEntity;

    /**
     * Callback interface for delivering parsed responses.
     */
    private Response.Listener mListener;

    /**
     * Represents the image to be uploaded in file format.
     */
    private File mFileImage;

    /**
     * Represents the image to be uploaded in its bitmap format.
     */
    private Bitmap mBitmap;

    private String mFolderPath;

    /**
     * Public constructor
     *
     * @param url URL of the API on where to upload your image
     * @param fileImage Image to be uploaded in file format
     * @param listener Listener if API image upload was a success
     * @param errorListener Listener if API image upload was a failure
     */
    public ImageUploadRequest(String url, File fileImage, Response.Listener listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);

        mListener = listener;

        mFileImage = fileImage;

        mHttpEntity = buildMultipartEntity();
    }

    /**
     * Public constructor
     *
     * @param url URL of the API on where to upload your image
     * @param bitmap Image to be uploaded in bitmap format
     * @param listener Listener if API image upload was a success
     * @param errorListener Listener if API image upload was a failure
     */
    public ImageUploadRequest(String url, Bitmap bitmap, Response.Listener listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);

        mListener = listener;

        mBitmap = bitmap;

        mHttpEntity = buildMultipartEntity();
    }

    public void setFolderPath(String folderPath) {
        mFolderPath = folderPath;
    }

    /**
     * Subclasses must implement this to perform delivery of the parsed
     * response to their listeners.  The given response is guaranteed to
     * be non-null; responses that fail to parse are not delivered.
     *
     * @param response The parsed response returned by
     * {@link #parseNetworkResponse(NetworkResponse)}
     */
    @Override
    public void deliverResponse(String response) {
        Log.i(TAG, "Image deliver response: " + response);
        mListener.onResponse(response);
    }

    /**
     * Subclasses must implement this to parse the raw network response
     * and return an appropriate response type. This method will be
     * called from a worker thread.  The response will not be delivered
     * if you return null.
     *
     * @param response Response from the network
     * @return The parsed response, or null in the case of an error
     */
    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String responseBody = "";
        try {
            responseBody = new String(response.data, "utf-8");
            Log.i(TAG, "Network response data: " + responseBody);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "Network response status: " + response.statusCode);

        return Response.success(responseBody, HttpHeaderParser.parseCacheHeaders(response));
    }

    /**
     * Returns a list of extra HTTP headers to go along with this request. Can
     * throw {@link AuthFailureError} as authentication may be required to
     * provide these values.
     *
     * @throws AuthFailureError In the event of auth failure
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();

        if(headers == null || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<>();
        }

        return headers;
    }

    /**
     * Returns a Map of parameters to be used for a POST or PUT request.  Can throw
     * {@link AuthFailureError} as authentication may be required to provide these values.
     *
     * <p>Note that you can directly override {@link #getBody()} for custom data.</p>
     *
     * @throws AuthFailureError in the event of auth failure
     */
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Log.i(TAG, "Image path: " + mFileImage.getPath());
        Log.i(TAG, "bytes: " + imageToBytes(mFileImage).length);

        // Encoded the image to be uploaded, either in its File or Bitmap format,
        // to String 64 encoded representation.
        String encodedImage = null;
        if(mFileImage != null) {
            encodedImage = Base64.encodeToString(imageToBytes(mFileImage), Base64.DEFAULT);
        } else if (mBitmap != null) {
            encodedImage = Base64.encodeToString(imageToBytes(mBitmap), Base64.DEFAULT);
        }

        // Attach the encoded String format of the image as a parameter,
        // to be sent to the API.
        Map<String, String> map = new HashMap<>();
        map.put("media", encodedImage);

        Log.i(TAG, "Image upload params: " + map.toString());
        return map;
    }

    /**
     * Returns the content type of the POST or PUT body.
     */
    @Override
    public String getBodyContentType() {
        return mHttpEntity.getContentType().getValue();
    }

    /**
     * Returns the raw POST or PUT body to be sent.
     *
     * <p>By default, the body consists of the request parameters in
     * application/x-www-form-urlencoded format. When overriding this method, consider overriding
     * {@link #getBodyContentType()} as well to match the new body format.
     *
     * @throws AuthFailureError in the event of auth failure
     */
    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            mHttpEntity.writeTo(byteStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteStream.toByteArray();
    }

    /**
     * Create an HTTP multipart/form-data enctype format to be passed on to the API.
     *
     * @return HTTP entity in multipart/form-data format
     */
    private HttpEntity buildMultipartEntity() {
        MultipartEntityBuilder multipartBuilder = MultipartEntityBuilder.create();

        if(mFileImage != null) {
            // If File is not null, it means image came from gallery
            multipartBuilder.addPart("media", new FileBody(mFileImage));
        } else {
            // If you are here, it means image came from camera
            File file = bitmapToFile(mBitmap, mFolderPath);
            multipartBuilder.addPart("media", new FileBody(file));
        }

        //multipartBuilder.addBinaryBody(KEY_IMAGE, fileImage, ContentType.create("image/jpeg"), imageFilename);
        multipartBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        //multipartBuilder.setLaxMode().setBoundary("xx").setChar

        return multipartBuilder.build();
    }

    /**
     * Converts the image in File format to byte representation.
     *
     * @param fileImage Image to be converted in File format.
     * @return Byte representation of the image
     */
    private byte[] imageToBytes(File fileImage) {
        Bitmap bm = BitmapFactory.decodeFile(fileImage.getPath());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 10, baos); //bm is the bitmap object
        return baos.toByteArray();
    }

    /**
     * Converts the image in Bitmap format to byte representation.
     *
     * @param bitmap Image to be converted in Bitmap format
     * @return Byte representation of the image
     */
    private byte[] imageToBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos); //bm is the bitmap object
        return baos.toByteArray();
    }

    /**
     * Resize and compress the image taken from the camera.
     *
     * @param bitmap Image to be compressed in Bitmap format
     * @param dirPath Path of the image
     * @return Compressed image in File format
     */
    private File bitmapToFile(Bitmap bitmap, String dirPath) {
        File dirResized = new File(dirPath);

        int origWidth = bitmap.getWidth();
        int origHeight = bitmap.getHeight();

        Bitmap bitmapOut = Bitmap.createScaledBitmap(bitmap, origWidth, origHeight, false);

        File fileResized = new File(dirResized, UUID.randomUUID().toString() + "-resized.png");
        Log.i("Yo", "Yo File size: " + bitmapOut.getByteCount());
        try {
            fileResized.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out;
        try {
            out = new FileOutputStream(fileResized);
            bitmapOut.compress(Bitmap.CompressFormat.JPEG, 30, out);

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileResized;
    }
}

package com.rullykj.productidentification;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.rullykj.productidentification.ml.Trained202300821Resnet1;
import com.rullykj.productidentification.rest.PostData;
import com.rullykj.productidentification.rest.RestClient;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import pl.aprilapps.easyphotopicker.ChooserType;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.MediaFile;
import pl.aprilapps.easyphotopicker.MediaSource;

/**
 * Created by Rully Kusumajaya on 8/13/2023.
 * Badan Riset dan Inovasi Nasional
 * rull001@brin.go.id
 * r.kusumajaya@liverpool.ac.uk
 */
public class MainActivity extends AppCompatActivity implements EasyImage.EasyImageStateHandler, View.OnClickListener {
    private Button bStartCamera;
    private Button bPredict;
    private TextView tvResult;
    private TextView tvResultLink;
    private ImageView imageView;
    private ArrayList<MediaFile> photos = new ArrayList<>();
    private EasyImage easyImage;
    String encodedImageBase64;
    boolean isMerge = false;

    boolean isOnline = true;
    private static final String PHOTOS_KEY = "easy_image_photos_list";
    private static final String STATE_KEY = "easy_image_state";
    private static final int CHOOSER_PERMISSIONS_REQUEST_CODE = 7459;
    private static final int GALLERY_REQUEST_CODE = 7502;
    private static final int DOCUMENTS_REQUEST_CODE = 7503;
    private static final int LEGACY_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 456;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 234;
    private static final String[] LEGACY_WRITE_PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final String[] CAMERA_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    //    private ImagesAdapter imagesAdapter;
    private Bundle easyImageState = new Bundle();


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        if (item.getItemId() == R.id.isMerge) {
            if (item.isChecked()) {
                item.setChecked(false);
                isMerge = false;
            } else {
                item.setChecked(true);
                isMerge = true;
                Toast.makeText(getApplication(), "Use Merge models", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bStartCamera = findViewById(R.id.bStart);
        bPredict = findViewById(R.id.bPredict);
        tvResult = findViewById(R.id.tvResult);
        tvResultLink = findViewById(R.id.tvResultLink);
        imageView = findViewById(R.id.imageView);

        if (savedInstanceState != null) {
            photos = savedInstanceState.getParcelableArrayList(PHOTOS_KEY);
            easyImageState = savedInstanceState.getParcelable(STATE_KEY);
        }

        easyImage = new EasyImage.Builder(this)
                .setChooserTitle("Pick media")
                .setCopyImagesToPublicGalleryFolder(true) // THIS requires granting WRITE_EXTERNAL_STORAGE permission for devices running Android 9 or lower
//                .setChooserType(ChooserType.CAMERA_AND_DOCUMENTS)
                .setChooserType(ChooserType.CAMERA_AND_GALLERY)
                .setFolderName("EasyImage sample")
                .allowMultiple(true)
                .setStateHandler(this)
                .build();

        bStartCamera.setOnClickListener(this);
        bPredict.setOnClickListener(this);


//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//                BitmapDrawable drawable = (BitmapDrawable) mCameraView.getDrawable();
//                Bitmap bitmap = drawable.getBitmap();
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
//                byte[] byteArray = stream.toByteArray();
//                encodedImageCamera = Base64.encodeToString(byteArray, Base64.DEFAULT);
//                PostData postData = new PostData(123,"verify", -6.347000, 106.697281, 0, timestamp.toString(), "1234", encodedImage, encodedImageCamera);
//                Gson gson = new Gson();
//                String jsonInString = gson.toJson(postData);
//                Single<String> single = RestClient.getBiometrikRetrofitService().verify(jsonInString);
//                //                Single<String> single = RestClient.getBiometrikRetrofitService().verify(
////                        123,"verify", -6.347000, 106.697281, 0, timestamp.toString(), "1234", encodedImage, encodedImageCamera
////                );
//                single.subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribeWith(new DisposableSingleObserver<String>() {
//                            @Override
//                            public void onSuccess(String str) {
//                                //dismissProgressDialog();
//                                // Received
//                                //Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
//                                textView4.setText(str);
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//                                // Network error
//                                Toast.makeText(MainActivity.this, "Koneksi bermasalah!", Toast.LENGTH_SHORT).show();
//                                //dismissProgressDialog();
//                            }
//                        });
//            }
//        });
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        // Forward results to EasyPermissions
//        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
//    }

//    private void toggleCameraView() {
//        if (imageView.getVisibility() == View.VISIBLE) {
//            imageView.setVisibility(View.GONE);
//            previewView.setVisibility(View.VISIBLE);
//        } else {
//            imageView.setVisibility(View.VISIBLE);
//            previewView.setVisibility(View.GONE);
//        }
//    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PHOTOS_KEY, photos);
        outState.putParcelable(STATE_KEY, easyImageState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openCameraForImage(MainActivity.this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        easyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onMediaFilesPicked(MediaFile[] imageFiles, MediaSource source) {
                for (MediaFile imageFile : imageFiles) {
                    Log.d("EasyImage", "Image file returned: " + imageFile.getFile().toString());
                }
                onPhotosReturned(imageFiles);
            }

            @Override
            public void onImagePickerError(@NonNull Throwable error, @NonNull MediaSource source) {
                //Some error handling
                error.printStackTrace();
            }

            @Override
            public void onCanceled(@NonNull MediaSource source) {
                //Not necessary to remove any files manually anymore
            }
        });
    }

    private void onPhotosReturned(@NonNull MediaFile[] returnedPhotos) {
        photos.addAll(Arrays.asList(returnedPhotos));
        Picasso.get()
                .load(photos.get(photos.size() - 1).getFile())
                .fit()
                .centerCrop()
                .into(imageView);

        //mCameraView.setImageBitmap()
        //imagesAdapter.notifyDataSetChanged();
        //recyclerView.scrollToPosition(photos.size() - 1);
        bPredict.setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public Bundle restoreEasyImageState() {
        return easyImageState;
    }

    @Override
    public void saveEasyImageState(Bundle state) {
        easyImageState = state;
    }

    private boolean isLegacyExternalStoragePermissionRequired() {
        boolean permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return Build.VERSION.SDK_INT < 29 && !permissionGranted;
    }

    private boolean isCameraPermissionRequired() {
        boolean permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        return !permissionGranted;
    }

    private void requestLegacyWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(MainActivity.this, LEGACY_WRITE_PERMISSIONS, LEGACY_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, CAMERA_PERMISSIONS, CAMERA_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onClick(View view) {
        tvResult.setText("");
        tvResultLink.setText("");
        int id = view.getId();
        if (id == R.id.bStart) {
            if (isLegacyExternalStoragePermissionRequired()) {
                requestLegacyWriteExternalStoragePermission();
            } else {
                if (isCameraPermissionRequired()) {
                    requestCameraPermission();
                } else {
                    easyImage.openCameraForImage(MainActivity.this);
                }
            }
        } else if (id == R.id.bPredict) {
            if (!(isOnline)) {
                predictOffline();
            } else if (isOnline) {
                predictOnline();
            }
        }
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        int BATCH_SIZE = 1;
        int inputSize = 224;
        int PIXEL_SIZE = 3;
        int IMAGE_MEAN = 128;
        float IMAGE_STD = 128.0f;

        ByteBuffer byteBuffer;
        byteBuffer = ByteBuffer.allocateDirect(
                4 * BATCH_SIZE * inputSize* inputSize * PIXEL_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[inputSize * inputSize];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat(
                        (((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                byteBuffer.putFloat(
                        (((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                byteBuffer.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
            }
        }
        return byteBuffer;
    }

    private int argMax(float[] array) {
        int maxIndex = 0;
        for (int i=0; i<array.length; i++) {
            if (array[maxIndex] < array[i]) {
                maxIndex = i;
            }
        }

        return maxIndex;
    }
    private void predictOffline(){
        try {
            Trained202300821Resnet1 model = Trained202300821Resnet1.newInstance(MainActivity.this);

            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, false);

            ByteBuffer inputBuffer = convertBitmapToByteBuffer(bitmap);
//            byte[] byteArray = stream.toByteArray();


            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(inputBuffer);

            // Runs model inference and gets result.
            Trained202300821Resnet1.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] data1 = outputFeature0.getFloatArray();
            String result = "";
            for (int i=0; i<data1.length; i++) {
                result+=", " + data1[i];
            }
            tvResult.setText(result);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    public int argmax(ArrayList<Double> array) {
        double max = array.get(0);
        int re = 0;
        for (int i = 1; i < array.size(); i++) {
            if (array.get(i) > max) {
                max = array.get(i);
                re = i;
            }
        }
        return re;
    }

    public Integer[] sortIndices(ArrayList<Double> input){

        Integer [] indices = new Integer[input.size()];
//        double [] output = new double[input.size()];

        for (int i = 0; i <input.size() ; i++) {
            indices[i] = i;
//            output[i] = input.get(i);
        }

        Arrays.sort(indices, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return Double.compare(input.get(o2), input.get(o1));
            }
        });

//        Arrays.sort(indices, new Comparator<Integer>() {
//            @Override
//            public int compare(Integer o1, Integer o2) {
//                return Double.compare(input.get(o1), input.get(o2));
//            }
//        });

//        System.out.println("Input Array: " + Arrays.toString(input));
//        System.out.println("Sorted indices as per input array: " + Arrays.toString(indices));
        return indices;
    }

    private void predictOnline(){
        //            Toast.makeText(MainActivity.this,"Predict Button Pressed!", Toast.LENGTH_SHORT).show();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, false);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        encodedImageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
//            Toast.makeText(MainActivity.this, encodedImageBase64.substring(20), Toast.LENGTH_SHORT).show();
        PostData postData = new PostData(123, "predict", timestamp.toString(), "1234", encodedImageBase64, isMerge);
        Gson gson = new Gson();
        String jsonInString = gson.toJson(postData);
//            Toast.makeText(MainActivity.this, jsonInString, Toast.LENGTH_SHORT).show();
        Single<String> single = RestClient.getProductRetrofitService().predict(jsonInString);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<String>() {
                    @Override
                    public void onSuccess(String str) {
                        //dismissProgressDialog();
                        // Received
                        //Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
//                            textView4.setText(str);
                        String output = "";
                        String outputLink = "";
                        try {
                            JSONObject jObject = new JSONObject(str);
                            String prediction = jObject.getString("prediction");
                            JSONArray jArrayProduct = jObject.getJSONArray("product");
                            JSONArray jArrayBrand = jObject.getJSONArray("brand");
                            JSONArray jArrayProductLabels = jObject.getJSONArray("product_labels");
                            JSONArray jArrayBrandLabels = jObject.getJSONArray("brand_labels");
                            ArrayList<Double> listdata = new ArrayList<>();
                            ArrayList<Double> listdataBrand = new ArrayList<>();
                            JSONArray jArrayScore = jArrayProduct.getJSONArray(0).getJSONArray(0);
                            if (jArrayScore != null) {
                                for (int i=0;i<jArrayScore.length();i++){
                                    listdata.add(jArrayScore.getDouble(i));
                                }
                            }
                            JSONArray jArrayBrandScore = jArrayBrand.getJSONArray(0).getJSONArray(0);
                            if (jArrayBrandScore != null) {
                                for (int i=0;i<jArrayBrandScore.length();i++){
                                    listdataBrand.add(jArrayBrandScore.getDouble(i));
                                }
                            }
                            Integer[] indices = sortIndices(listdata);
                            Integer[] brandIndices = sortIndices(listdataBrand);
                            output = jArrayProductLabels.getString(indices[0]).toUpperCase()+" : "+String.format("%.2f", (jArrayScore.getDouble(indices[0])*100
                            )) +"%"+System.getProperty("line.separator")+
                                    jArrayProductLabels.getString(indices[1]).toUpperCase()+" : "+String.format("%.2f", (jArrayScore.getDouble(indices[1])*100
                            )) +"%"+System.getProperty("line.separator")+
                                    jArrayProductLabels.getString(indices[2]).toUpperCase()+" : "+String.format("%.2f", (jArrayScore.getDouble(indices[2])*100
                            )) +"%"+System.getProperty("line.separator")+System.getProperty("line.separator")+ "BRAND : "+ System.getProperty("line.separator")+
                                    jArrayBrandLabels.getString(brandIndices[0]).toUpperCase()+" : "+String.format("%.2f", (jArrayBrandScore.getDouble(brandIndices[0])*100
                            )) +"%"+System.getProperty("line.separator")+
                                    jArrayBrandLabels.getString(brandIndices[1]).toUpperCase()+" : "+String.format("%.2f", (jArrayBrandScore.getDouble(brandIndices[1])*100
                            )) +"%"+System.getProperty("line.separator");

                            if(brandIndices[0] == 0){
                                outputLink = "https://www.ikea.com/gb/en/search/?q=";
                            }else{
                                outputLink = "https://www.ruparupa.com/informastore/jual/";
                            }
                            if(indices[0] == 3){
                                if(brandIndices[0] == 0){
                                    outputLink = outputLink+"swivel%20chair";
                                }else{
                                    outputLink = outputLink+"swivel-chair";
                                }
                            }else{
                                outputLink = outputLink+jArrayProductLabels.getString(indices[0]);
                            }

//                            str = ""+indices;
//                            str = str + " https://www.ikea.com/gb/en/search/?q=" + prediction;
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        tvResult.setText(output);
                        tvResultLink.setText(outputLink);
//                            tvResult.setMovementMethod(LinkMovementMethod.getInstance());
                        Linkify.addLinks(tvResultLink, Linkify.ALL);
                    }

                    @Override
                    public void onError(Throwable e) {
                        // Network error
                        Toast.makeText(MainActivity.this, "Koneksi bermasalah!", Toast.LENGTH_SHORT).show();
                        //dismissProgressDialog();
                    }
                });
    }
}
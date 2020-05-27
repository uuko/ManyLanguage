package com.example.capturevideoandpictureandsaveandchoose.ui.main;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capturevideoandpictureandsaveandchoose.Application;
import com.example.capturevideoandpictureandsaveandchoose.MagicFileChooser;
import com.example.capturevideoandpictureandsaveandchoose.R;
import com.example.capturevideoandpictureandsaveandchoose.base.BaseActivity;
import com.example.capturevideoandpictureandsaveandchoose.di.component.main.DaggerMainComponent;
import com.example.capturevideoandpictureandsaveandchoose.di.component.main.MainComponent;
import com.example.capturevideoandpictureandsaveandchoose.di.module.main.MainModule;
import com.example.capturevideoandpictureandsaveandchoose.ui.choosedevice.ChooseDeviceActivity;
import com.example.capturevideoandpictureandsaveandchoose.ui.choosedevice.ChooseDeviceItemData;
import com.example.capturevideoandpictureandsaveandchoose.ui.deviceinformation.DeviceInformationActivity;
import com.example.capturevideoandpictureandsaveandchoose.utils.api.apidata.searcheqkd.EQKDRequest;
import com.example.capturevideoandpictureandsaveandchoose.utils.api.apidata.searcheqno.EQNORequest;
import com.example.capturevideoandpictureandsaveandchoose.utils.api.apidata.searcheqno.EQNOResponse;
import com.example.capturevideoandpictureandsaveandchoose.utils.api.apidata.searchpmfct.PMFCTRequest;
import com.example.capturevideoandpictureandsaveandchoose.utils.service.NonInspectionService;
import com.example.capturevideoandpictureandsaveandchoose.utils.service.TeleportService;
import com.guoxiaoxing.phoenix.compress.video.VideoCompressor;
import com.guoxiaoxing.phoenix.compress.video.format.MediaFormatStrategyPresets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends BaseActivity implements MainContract.View, View.OnClickListener {
    @Inject
    MainContract.Presenter<MainContract.View> mPresenter;
    private static final int REQUEST_CAPTURE_IMAGE = 100;
    private static final int REQUEST_VIDEO_CAPTURE = 200;
    private static final int PICK_IMAGE_FROM_GALLERY_REQUEST_CODE = 300;
    private static final int PICK_VIDEO_FROM_GALLERY_REQUEST_CODE = 400;
    private static final int PICK_FILE_REQUEST_CODE = 500;
    private static final int DEVICE_INFORMATION = 600;
    private static final int GET_DEVICE_DATA = 2021;
    private ArrayList<ChooseDeviceItemData> deviceDataList;


    Handler handler = new Handler();

    String imageFilePath;
    private Button btnCapturePicture, btnRecordVideo, btnGetImageFromGallery, btnGetVideoFromGallery, btnDeviceEdit, btnCentralCloud, btnChoseDevice,btnBasicInformation;
    private TextView textDeviceNumber;
    private File photoFile;
    private MainComponent mMainComponent;
    final String sn = android.os.Build.SERIAL;
    int countFile = 0;
    int deviceDataPosition;
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        mPresenter.onAttached(this);
        onStartTeleportService();
        mPresenter.onGetDisposableToken(sn);
    }

    @Override
    public void init() {
        btnCentralCloud = findViewById(R.id.btn_central_cloud);
        btnCapturePicture = findViewById(R.id.btnCaptureImage);
        textDeviceNumber = findViewById(R.id.text_device_number_data);
        btnRecordVideo = findViewById(R.id.btnRecordVideo);
        btnGetImageFromGallery = findViewById(R.id.btnGetImageFromGallery);
        btnGetVideoFromGallery = findViewById(R.id.btnGetVideoFromGallery);
        btnDeviceEdit = findViewById(R.id.btn_device_edit);
        btnChoseDevice = findViewById(R.id.btn_chose_device);
        btnBasicInformation = findViewById(R.id.btn_basic_information);
        deviceDataList = new ArrayList<>();
        mMainComponent = DaggerMainComponent.builder()
                .mainModule(new MainModule(this))
                .baseComponent(((Application) getApplication()).getApplicationComponent())
                .build();
        mMainComponent.inject(this);

        Intent serviceIntent = new Intent(this, TeleportService.class);
        startService(serviceIntent);
        btnCapturePicture.setOnClickListener(this);
        btnRecordVideo.setOnClickListener(this);
        btnGetImageFromGallery.setOnClickListener(this);
        btnGetVideoFromGallery.setOnClickListener(this);
        btnDeviceEdit.setOnClickListener(this);
        btnCentralCloud.setOnClickListener(this);
        btnChoseDevice.setOnClickListener(this);
        btnBasicInformation.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("gggg", "getStringExtra:" + countFile + ":" + intent.getStringExtra("Data"));
        }
    };

    private void pickImageFromGallery() {

        //Create an Intent with action as ACTION_PICK
        Intent intent = new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        // Launching the Intent
        startActivityForResult(intent, PICK_IMAGE_FROM_GALLERY_REQUEST_CODE);
    }

    private void pickVideoFromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        String[] mimeTypes = {"video/mp4", "video/mov"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, PICK_VIDEO_FROM_GALLERY_REQUEST_CODE);

    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
//        String[] mimeTypes = {"application/pdf"};
//        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    private void openCameraIntent() {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        settingSystemCamera(pictureIntent);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            //Create a file to store the image
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.capturevideoandpictureandsaveandchoose.provider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                startActivityForResult(pictureIntent,
                        REQUEST_CAPTURE_IMAGE);
            }
        }
    }

    private void openRecordVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        PackageManager packageManager = this.getPackageManager();
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(takeVideoIntent, 0);
        takeVideoIntent.setPackage(listCam.get(0).activityInfo.packageName);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private void deviceInformation() {
        Intent it = new Intent(this, DeviceInformationActivity.class);
        if(deviceDataList.size() != 0 && !textDeviceNumber.getText().equals("")) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("device", deviceDataList.get(deviceDataPosition));
            it.putExtra("NonInspectionWorkDevice",deviceDataList);
            it.putExtra("device", deviceDataList.get(deviceDataPosition));
        }
        startActivityForResult(it, DEVICE_INFORMATION);
    }

    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }

    private void settingSystemCamera(Intent intent) {
        PackageManager packageManager = MainActivity.this.getPackageManager();
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(intent, 0);
        intent.setPackage(listCam.get(0).activityInfo.packageName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            File imgFile = new File(imageFilePath);
            //照片的檔案
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                try {
                    FileOutputStream out = new FileOutputStream(imgFile);
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            //影片的uri
            Uri videoUri = data.getData();
        }

        if (requestCode == PICK_IMAGE_FROM_GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getClipData().getItemAt(0).getUri();
            ArrayList<String> uriList = new ArrayList<String>();
            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                uriList.add(getPath(data.getClipData().getItemAt(i).getUri()));
                Log.e("gggg", "" + uriList.get(i));
            }
            Log.e("gggg", "1:" + data.getClipData().getItemCount());
            //照片的uri
            onUploadFile(uriList, getResourceString(R.string.on_upload_image));
        }

        if (requestCode == PICK_VIDEO_FROM_GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri selectedVideo = data.getData();
            ArrayList<String> uriList = new ArrayList<String>();
            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                uriList.add(getPath(data.getClipData().getItemAt(i).getUri()));
                Log.e("gggg", "" + uriList.get(i));
            }
            // TODO here====================
            ArrayList<String> compressList = new ArrayList<>();
            compressList.addAll(compressVideo(uriList));
            countFile = 0;


            //先註解測試影片壓縮功能
            onUploadFile(compressList, getResourceString(R.string.on_upload_vedio));
            //影片的uri
        }

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri selectedFile = data.getData();
            Log.e("ggggg", "" + selectedFile);
            String filePath = MagicFileChooser.getAbsolutePathFromUri(this, selectedFile);
            Log.e("filePath", filePath + "");

            checkFileTypeAndOpen(filePath, selectedFile);

        }

        if (requestCode == GET_DEVICE_DATA && resultCode == RESULT_OK) {
            deviceDataList = (ArrayList<ChooseDeviceItemData>) data.getSerializableExtra("NonInspectionWorkDevice");
        }

        if (requestCode == DEVICE_INFORMATION && resultCode == RESULT_OK) {
            deviceDataList = (ArrayList<ChooseDeviceItemData>) data.getSerializableExtra("NonInspectionWorkDevice");
        }
    }

    //根據副檔名判斷用什麼應用程式開啟
    private void checkFileTypeAndOpen(String filePath, Uri selectedFile) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (filePath.contains(".doc")) {
            intent.setDataAndType(selectedFile, "application/msword");
        } else if (filePath.contains(".ppt")) {
            intent.setDataAndType(selectedFile, "application/vnd.ms-powerpoint");
        } else if ((filePath.contains(".csv")) || (filePath.contains(".xls"))) {
            intent.setDataAndType(selectedFile, "application/vnd.ms-excel");
        } else if (filePath.contains(".pdf")) {
            intent.setDataAndType(selectedFile, "application/pdf");
        } else if (filePath.contains(".txt")) {
            intent.setDataAndType(selectedFile, "text/plain");
        } else {
            intent.setDataAndType(selectedFile, "application/*");
        }

        PackageManager pm = getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
        if (list.size() > 0)
            startActivity(intent);
    }

    private ArrayList compressVideo(ArrayList<String> uriList) {
        final ArrayList<String> returnList = new ArrayList<>();
        final ArrayList<File> inputFileList = new ArrayList<>();
        final ArrayList<File> compressFileList = new ArrayList<>();
        for (int i = 0; i < uriList.size(); i++) {
            inputFileList.add(i, new File(uriList.get(i)));
        }
        int countNeedToCompress = 0;
        for (int i = 0; i < uriList.size(); i++) {
            try {
                File compressCachePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "phoenix");
                compressCachePath.mkdir();
                Log.e("input", inputFileList.get(i).length() + "");
                if (inputFileList.get(i).length() / 1048576 >= 50) {
                    compressFileList.add(countNeedToCompress, File.createTempFile("compress" + i, ".mp4", compressCachePath));
                    countNeedToCompress++;

                } else {
                    returnList.add(uriList.get(i));
                }
            } catch (IOException e) {
                Toast.makeText(this, "Failed to create temporary file.", Toast.LENGTH_LONG).show();
                return null;
            }
        }


        VideoCompressor.Listener listener = new VideoCompressor.Listener() {
            @Override
            public void onTranscodeProgress(double progress) {
            }

            @Override
            public void onTranscodeCompleted() {
                String compressPath = compressFileList.get(countFile).getAbsolutePath();
                returnList.add(compressPath);
                Log.e("afterCompress", "" + compressFileList.get(countFile).length());
                Log.e("compressPath", "" + compressPath);
                countFile++;

            }

            @Override
            public void onTranscodeCanceled() {

            }

            @Override
            public void onTranscodeFailed(Exception exception) {

            }
        };


        for (int i = 0; i < compressFileList.size(); i++) {
            try {
                VideoCompressor.with().asyncTranscodeVideo(uriList.get(i), compressFileList.get(i).getAbsolutePath(),
                        MediaFormatStrategyPresets.createAndroid480pFormatStrategy(), listener);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return returnList;

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_basic_information:
                deviceInformation();
                break;

            case R.id.btnCaptureImage:
                openCameraIntent();
                break;

            case R.id.btnRecordVideo:
                openRecordVideoIntent();
                break;

            case R.id.btnGetImageFromGallery:
                pickImageFromGallery();
                break;

            case R.id.btnGetVideoFromGallery:
                pickVideoFromGallery();
                break;
            case R.id.btn_central_cloud:
                Intent intentCentralCloud = new Intent();
                intentCentralCloud.setAction(Intent.ACTION_VIEW);
                intentCentralCloud.setData(Uri.parse("https://www.google.com/"));
                startActivity(intentCentralCloud);
                break;
            case R.id.btn_device_edit:
                Intent intent = new Intent(this, ChooseDeviceActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("deviceDataList", deviceDataList);
                intent.putExtras(bundle);
                startActivityForResult(intent, GET_DEVICE_DATA);
                break;
            case R.id.btn_chose_device:
                ArrayList<String> dialogDeviceIDString = new ArrayList<>();
                for (ChooseDeviceItemData deviceData : deviceDataList) {
                    dialogDeviceIDString.add(deviceData.getDeciceId());
                }
                showItemDialog(dialogDeviceIDString, onNonInspectionSelectDevice);
                break;
        }
    }

    private void onStartTeleportService() {
        Intent intent = new Intent(this, TeleportService.class);
        Bundle serviceBundle = new Bundle();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("datatest");
        serviceBundle.putString("aa", "成功");
        intent.putExtras(serviceBundle);
        this.startService(intent);
    }

    private void onStartNonInspectionService() {
        //非巡檢時用 用來
        Intent intent = new Intent(this, NonInspectionService.class);
        Bundle serviceBundle = new Bundle();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("NonInspection");
        serviceBundle.putSerializable("chooseDeviceData", deviceDataList);
        intent.putExtras(serviceBundle);
        this.startService(intent);
    }
    int runStatus = 0;
    int lastPosition = 0;
    int insert = 0;

    private DialogInterface.OnClickListener onNonInspectionSelectDevice = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            handler.removeCallbacks(updateDevice);
            handler.removeCallbacks(endDevice);
            if(runStatus == 1){
                lastPosition = deviceDataPosition;
                deviceDataPosition = which;
                handler.postDelayed(insertDevice, 3000);
            }else{
                deviceDataPosition = which;
                handler.postDelayed(updateDevice, 100);
            }
            Log.e("gggg", "" + deviceDataList.get(which).getDeciceId());
//            textDeviceNumber.setText(deviceDataList.get(which).getDeciceId());
        }
    };

    private Runnable updateDevice = new Runnable() {
        public void run() {
            handler.removeCallbacks(insertDevice);
            runStatus = 1;
            if(deviceDataPosition == deviceDataList.size()-1){
                Log.v("777777777","1111111111111111" + deviceDataPosition);
                textDeviceNumber.setText(deviceDataList.get(deviceDataPosition).getDeciceId());
                handler.postDelayed(endDevice, 5000);
            }else{
                Log.v("777777777","1111111111111111" + deviceDataPosition);
                textDeviceNumber.setText(deviceDataList.get(deviceDataPosition).getDeciceId());
                handler.postDelayed(this, 3000);
                deviceDataPosition++;
            }
        }
    };

    private Runnable endDevice = new Runnable() {
        public void run() {
            Log.v("777777777","1111111111111111" + "2323");
            textDeviceNumber.setText("stop");
            runStatus = 0;
            handler.removeCallbacks(updateDevice);
            handler.removeCallbacks(endDevice);
        }
    };

    private Runnable insertDevice = new Runnable() {
        public void run() {
            if(deviceDataPosition == deviceDataList.size()-1){
                Log.v("777777777","1111111111111111" + deviceDataPosition);
                textDeviceNumber.setText(deviceDataList.get(deviceDataPosition).getDeciceId());
            }else{
                Log.v("777777777","1111111111111111" + deviceDataPosition);
                textDeviceNumber.setText(deviceDataList.get(deviceDataPosition).getDeciceId());
            }
            deviceDataPosition = lastPosition;
            handler.postDelayed(updateDevice, 3000);
        }
    };

    //如果能改成用retrofit加rxjava最好，已經嘗試過三天的，可能有缺什麼，不過緊急所以先求功能
    private void onUploadFile(final ArrayList<String> uriList, String type) {
        showProgressDialog(type);
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MediaType mediaType = MediaType.parse("text/plain");
                MultipartBody.Builder buildernew = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("AuthorizedId", "1179cf63-9f4c-4060-a0f3-201f108b20c1")
                        .addFormDataPart("CO", "1")
                        .addFormDataPart("CONM", "台塑")
                        .addFormDataPart("PMFCT", "A3")
                        .addFormDataPart("PMFCTNM", "麥寮AN廠")
                        .addFormDataPart("EQKD", "PU")
                        .addFormDataPart("EQKDNM", "泵浦")
                        .addFormDataPart("EQNO", "P-166")
                        .addFormDataPart("EQNM", "工業用水泵浦")
                        .addFormDataPart("RecordDate", "2020/04/06")
                        .addFormDataPart("RecordSubject", "測試")
                        .addFormDataPart("UploadEMP", "1")
                        .addFormDataPart("UploadNM", "新人")
                        .addFormDataPart("UploadDATETM", "");
                for (String path : uriList) {
                    File uploadFile = new File(path);
                    buildernew.addFormDataPart("", uploadFile.getName(),
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    uploadFile));
                }
                RequestBody body = buildernew.build();

                Request request = new Request.Builder()
                        .url("https://cloud.fpcetg.com.tw/FPC/API/MTN/API_MTN/MTN/Upload")
                        .method("POST", body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    Log.e("response", response.body().string());
                    dismissProgressDialog();
//                        Log.e("isSuccess",json.get("IsSuccess").toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    dismissProgressDialog();
                    showDialogCaveatMessage("上傳失敗");
                    Log.e("error", "" + e.getMessage());
                }
            }
        }).start();
    }

}

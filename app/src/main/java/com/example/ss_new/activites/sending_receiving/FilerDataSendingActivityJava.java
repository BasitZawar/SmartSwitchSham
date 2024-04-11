package com.example.ss_new.activites.sending_receiving;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.ss_new.activites.DashboardActivity;
import com.example.ss_new.activites.FileToSelectActivity;
import com.example.ss_new.app_utils.AllFilesUtils;
import com.example.ss_new.app_utils.data_classes.ss_models.TransferModel;
import com.example.ss_new.database.DBHelper;
import com.example.ss_new.database.FilesEntity;
import com.example.ss_new.databinding.ActivitySenderBinding;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import timber.log.Timber;

public class FilerDataSendingActivityJava extends AppCompatActivity {


    private static final String TAG = FilerDataSendingActivityJava.class.getCanonicalName();
    private ActivitySenderBinding binding;
    private ArrayList<TransferModel> mArrayList = new ArrayList<>();
    private long currentDataSizeSent = 0;

    private int selectedAudios = 0;
    private int selectedVideos = 0;
    private int selectedImages = 0;
    private int selectedDocs = 0;
    private int selectedApps = 0;
    private int selectedDowns = 0;
    private int selectedApks = 0;

    private int sentVideoCount = 0;
    private int sentAudioCount = 0;
    private int sentImgCount = 0;
    private int sentDocCount = 0;
    private int sentDownCount = 0;
    private int sentAppCount = 0;
    private int sentApkCount = 0;

    private String toDayDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySenderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new LoadDataTask().execute();

        binding.btnBack.setOnClickListener(v -> onBackPressed());
        binding.btnDone.setOnClickListener(v -> {
            startActivity(new Intent(FilerDataSendingActivityJava.this, DashboardActivity.class));
            finishAffinity();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectWifiDirect();
    }

    private void disconnectWifiDirect() {
        WifiP2pManager p2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel p2pChannel = p2pManager.initialize(this, Looper.getMainLooper(), null);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        p2pManager.requestGroupInfo(p2pChannel, group -> {
            if (group != null) {
                p2pManager.removeGroup(p2pChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Timber.e(TAG, "removeGroup onSuccess -");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Timber.e("$TAG removeGroup onFailure -$reason");
                    }
                });
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setDataToSendingContent() {
        binding.progressVideo.setMax(selectedVideos);
        binding.tvTotalVideoSending.setText(selectedVideos + " items ");

        binding.progressImage.setMax(selectedImages);
        binding.tvTotalImagesSending.setText(selectedImages + " items ");

        binding.progressMusic.setMax(selectedAudios);
        binding.tvTotalMusicSending.setText(selectedAudios + " items ");

        binding.progressDoc.setMax(selectedDocs);
        binding.tvTotalDocSending.setText(selectedDocs + " items ");

        binding.progressDownload.setMax(selectedDowns);
        binding.tvTotalDownloadSending.setText(selectedDowns + " items ");

        binding.progressApp.setMax(selectedApps);
        binding.tvTotalAppSending.setText(selectedApps + " items ");

        binding.progressApk.setMax(selectedApks);
        binding.tvTotalApkSending.setText(selectedApks + " items ");
    }

    private class ShareDataTask extends AsyncTask<Void, Void, Void> {
        @SuppressLint("SetTextI18n")
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Socket socket =new Socket();
                if (socket != null) {
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    DataOutputStream dos = new DataOutputStream(oos);
                    dos.writeInt(mArrayList.size());
                    dos.writeUTF("video:" + selectedVideos + " img:" + selectedImages + " audio:" + selectedAudios + " doc:" + selectedDocs + " apps:" + selectedApps + " download:" + selectedDowns + " apk:" + selectedApks);
                    for (int i = 0; i < mArrayList.size(); i++) {
                        try {
                            if (mArrayList.get(i).getType().equals("Con")) {
                                // Handle Contacts
                            } else {
                                File currentFile = new File(mArrayList.get(i).getPath());
                                currentDataSizeSent += currentFile.length();

                                try {
                                    byte[] bytes = new byte[(int) currentFile.length()];
                                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(currentFile));
                                    bis.read(bytes, 0, bytes.length);
                                    oos.writeObject(mArrayList.get(i));
                                    oos.writeObject(bytes);
                                } catch (Exception ex) {
                                    Timber.e("$TAG failure $ex");
                                }
                                DBHelper.Db.getDB(FilerDataSendingActivityJava.this).sSwitchDao().insertAllFiles(
                                        new FilesEntity(0, mArrayList.get(i).getPath(), "", AllFilesUtils.INSTANCE.getFileType(new File(mArrayList.get(i).getPath())),
                                                false,
                                                true,
                                                true, toDayDate)
                                );

                                int finalI = i;
                                runOnUiThread(() -> {
                                    switch (mArrayList.get(finalI).getType()) {
                                        case AllFilesUtils.video:
                                            sentVideoCount += 1;
                                            updateVideoSendingProgress();
                                            break;
                                        case AllFilesUtils.image:
                                            sentImgCount += 1;
                                            updateImageSendingProgress();
                                            break;
                                        case AllFilesUtils.audio:
                                            sentAudioCount += 1;
                                            updateMusicSendingProgress();
                                            break;
                                        case AllFilesUtils.docs:
                                            sentDocCount += 1;
                                            updateDocSendingProgress();
                                            break;
                                        case AllFilesUtils.apk:
                                            sentApkCount += 1;
                                            updateApkSendingProgress();
                                            break;
                                        case AllFilesUtils.app:
                                            sentAppCount += 1;
                                            updateAppsSendingProgress();
                                            break;
                                        case AllFilesUtils.download:
                                            sentDownCount += 1;
                                            updateDownloadSendingProgress();
                                            break;
                                    }
                                });
                            }
                        } catch (Exception ex) {
                            System.out.println(ex);
                        } finally {
                            try {
                                oos.flush();
                                oos.reset();
                            } catch (Exception ex) {
                                Timber.e("$TAG run: $ex");
                            }
                        }
                    }
                    runOnUiThread(() -> binding.btnDone.setVisibility(View.VISIBLE));
                    try {
                        oos.close();
                        socket.close();
                    } catch (Exception ex) {
                        binding.btnDone.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception ex) {
                binding.btnDone.setVisibility(View.VISIBLE);
            }
            return null;
        }
    }

    private void updateVideoSendingProgress() {
        runOnUiThread(() -> binding.progressVideo.setProgress(sentVideoCount));
    }

    private void updateImageSendingProgress() {
        runOnUiThread(() -> binding.progressImage.setProgress(sentImgCount));
    }

    private void updateDocSendingProgress() {
        runOnUiThread(() -> binding.progressDoc.setProgress(sentDocCount));
    }

    private void updateMusicSendingProgress() {
        runOnUiThread(() -> binding.progressMusic.setProgress(sentAudioCount));
    }

    private void updateAppsSendingProgress() {
        runOnUiThread(() -> binding.progressApp.setProgress(sentAppCount));
    }

    private void updateApkSendingProgress() {
        runOnUiThread(() -> binding.progressApk.setProgress(sentApkCount));
    }

    private void updateDownloadSendingProgress() {
        runOnUiThread(() -> binding.progressDownload.setProgress(sentDownCount));
    }

    private class LoadDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            for (FilesEntity file : FileToSelectActivity.Companion.getSelectedFileList()) {
                switch (file.getFileType()) {
                    case AllFilesUtils.audio:
                        selectedAudios += 1;
                        break;
                    case AllFilesUtils.video:
                        selectedVideos += 1;
                        break;
                    case AllFilesUtils.docs:
                        selectedDocs += 1;
                        break;
                    case AllFilesUtils.image:
                        selectedImages += 1;
                        break;
                    case AllFilesUtils.apk:
                        selectedApks += 1;
                        break;
                    case AllFilesUtils.app:
                        selectedApps += 1;
                        break;
                    case AllFilesUtils.download:
                        selectedDowns += 1;
                        break;
                }
            }

            for (FilesEntity file : FileToSelectActivity.Companion.getSelectedFileList()) {
                if (file.getFolder().equals(AllFilesUtils.download)) {
                    mArrayList.add(new TransferModel(new File(file.getPath()).getName(), file.getPath(), AllFilesUtils.download));
                } else {
                    mArrayList.add(new TransferModel(new File(file.getPath()).getName(), file.getPath(), file.getFileType()));
                }
            }

            mArrayList.add(new TransferModel("Contacts", "Contacts", "Contacts"));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setDataToSendingContent();
            toDayDate = AllFilesUtils.INSTANCE.getCurrentDate();
            new ShareDataTask().execute();
        }
    }

}


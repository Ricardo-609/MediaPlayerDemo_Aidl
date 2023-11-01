package com.ricardo.MediaPlayerDemo_Aidl.mediaservice;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ricardo.MediaPlayerDemo_Aidl.mediaservice.IMedia;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaService extends Service {

    private MediaPlayer mediaPlayer;
    List<String> allSongLists = new ArrayList<>();
    private int curidx = 0, oldidx = 0;
    private String sd_path = null;

    @Override
    public void onCreate() {
        super.onCreate();
//        Log.d("Service : ", "started");
//        Log.d("Class Name : ", getClass().toString());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private IMedia.Stub mBinder = new IMedia.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public int getDuration() throws RemoteException {
            return mediaPlayer == null ? 0 : mediaPlayer.getDuration();
        }

        @Override
        public int getCurDuration() throws RemoteException {
            return mediaPlayer == null ? 0 : mediaPlayer.getCurrentPosition();
        }

        @Override
        public void setProgress(int pos) throws RemoteException {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(pos);
            }
        }

        @Override
        public List<String> getAllSong(String path) throws RemoteException {
            sd_path = path;
            File folder = new File(path);
            if (folder.exists()) {
                File[] files = folder.listFiles();
                for (int i = 0; i < files.length; ++i) {
                    if (files[i].getName().startsWith(".")) {
                        continue;
                    }
                    allSongLists.add(files[i].getName()); //add : xxx.mp3
                }
            }
            return allSongLists;
        }

        @Override
        public int getAllSongSize(String sdcard_file) throws RemoteException {
            return allSongLists.size();
        }

        private void initMediaPlayer() {
            if (mediaPlayer == null) {
                Uri uri = Uri.parse(sd_path + "/" + allSongLists.get(curidx));
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        Log.d("222222222222222222", "end");
                        oldidx = curidx;
                        curidx = (curidx + 1) % allSongLists.size();
                        mediaPlayer.reset();
                        try {
                            mediaPlayer.setDataSource(sd_path + "/" + allSongLists.get(curidx));
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        @Override
        public void changedSong(int idx) throws RemoteException {
            initMediaPlayer();

            if (curidx == idx) {
            } else if (idx != curidx) {
                oldidx = curidx;
                curidx = idx;

                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(sd_path + "/" + allSongLists.get(curidx));
                    mediaPlayer.prepare();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mediaPlayer.start();

        }

        @Override
        public void play_pause() throws RemoteException {
            initMediaPlayer();
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();

            }
        }

        @Override
        public int getIndex() throws RemoteException {
            return curidx;
        }



    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}

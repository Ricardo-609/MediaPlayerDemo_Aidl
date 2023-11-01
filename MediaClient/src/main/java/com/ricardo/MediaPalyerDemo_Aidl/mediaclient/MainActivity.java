package com.ricardo.MediaPalyerDemo_Aidl.mediaclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.ricardo.MediaPalyerDemo_Aidl.mediaclient.adapter;
import com.ricardo.MediaPlayerDemo_Aidl.mediaservice.IMedia;
import com.ricardo.MediaPlayerDemo_Aidl.mediaservice.MediaService;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private adapter adapter;
    private SeekBar seekBar;
    private TextView music_cur, music_length;
    private TextView textView1, textView2;
    private Button pre, next, play_pause;
    private List<TextView> textViews;


    private IMedia mediaService;
    private boolean isServiceBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mediaService = IMedia.Stub.asInterface(iBinder);
            isServiceBound = true;
//            Log.d("ServiceConnection : ", String.valueOf(serviceConnection));
            try {
                SongList.addAll(mediaService.getAllSong(sdcard_file));
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            adapter.setLIst(SongList);
            adapter.notifyDataSetChanged();

            try {
                cur_pos = mediaService.getIndex();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            //获取歌曲总数
            max_pos = adapter.getItemCount();

            updateHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //获取歌曲总数
//                    max_pos = SongList.size();

                    //改变当前进度条的值
                    //设置当前进度
                    try {
                        seekBar.setProgress(mediaService.getCurDuration());
                        music_cur.setText(format.format(mediaService.getCurDuration()));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }, 500);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mediaService = null;
            isServiceBound = false;
        }
    };

    SimpleDateFormat format;

    String sdcard_file = Environment.getExternalStorageDirectory().getPath() + "/music";   //获取sdcard路径
    List<String> SongList = new ArrayList<>();

    //播放cur_pos + 1首歌，歌曲总数max_pos
    private int cur_pos = 0, max_pos = 0;

    //切换歌曲时更新信息
    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    //用于更新进度条
    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find id
        init();

//        Log.d("Package name : ", getPackageName());
//        Log.d("Class Name : ", getClass().toString());

        //activity加载后进行服务绑定
        //方式一
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(
                "com.ricardo.mediaplayerdemo_aidl",     //包名通过getPackageName()获得
                "com.ricardo.MediaPlayerDemo_Aidl.mediaservice.MediaService"    //类名通过getClass().toString()获得
        ));
        //方式二
//        Intent intent = new Intent(this, MediaService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);


        adapter = new adapter(createTextViews());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        adapter.setOnItemClickListener(new adapter.OnItemClickListener() {
            @Override
            public void onItemOnClick(View view, int pos) {
//                Log.d("----------", "clicked--------------");
                try {
                    mediaService.changedSong(pos);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
//                cur_pos = pos;
                startBarUpdate();
            }
        });


        pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //取余，做循环
                cur_pos = (cur_pos - 1 + max_pos) % max_pos;

                try {
                    mediaService.changedSong(cur_pos);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                startBarUpdate();

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cur_pos = (cur_pos + 1) % max_pos;
                try {
                    mediaService.changedSong(cur_pos);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                startBarUpdate();
            }
        });

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mediaService.play_pause();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                startBarUpdate();

            }
        });


//        seekBar.setEnabled(true);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    try {
                        mediaService.setProgress(i);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    music_cur.setText(format.format(i));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void startBarUpdate() {
        mhandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    cur_pos = mediaService.getIndex();
                    seekBar.setProgress(mediaService.getCurDuration());
                    music_cur.setText(format.format(mediaService.getCurDuration()));
                    //设置进度条最大长度
                    music_length.setText(format.format(mediaService.getDuration()));
                    seekBar.setMax(mediaService.getDuration());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                //歌曲名/歌手
                String string = SongList.get(cur_pos);
                textView1.setText(string.substring(string.indexOf("-") + 1, string.indexOf(".")));
                textView2.setText(string.substring(0, string.indexOf("-")));

//                seekBar.setMin(0);

                mhandler.postDelayed(this, 100);

            }
        });
    }

    private void init() {

        requestForPermission();
//        //高版本sdk读取sd卡内容需要
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            if (!Environment.isExternalStorageManager()) {
//                startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
//            }
//        }

        format = new SimpleDateFormat("mm:ss");

        mRecyclerView = findViewById(R.id.recyclerView);
        seekBar = findViewById(R.id.seekBar);

        music_cur = findViewById(R.id.music_cur);
        music_length = findViewById(R.id.music_length);
        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);

        pre = findViewById(R.id.pre);
        next = findViewById(R.id.next);
        play_pause = findViewById(R.id.play_pause);
    }


    private List<TextView> createTextViews() {
        textViews = new ArrayList<>();

        for (int i = 0; i < max_pos; ++i) {
            textViews.add(new TextView(this));
        }
        return textViews;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    public final String[] EXTERNAL_PERMS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public final int EXTERNAL_REQUEST = 138;



    public boolean requestForPermission() {

        boolean isPermissionOn = true;
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            if (!canAccessExternalSd()) {
                isPermissionOn = false;
                requestPermissions(EXTERNAL_PERMS, EXTERNAL_REQUEST);
            }
        }

        return isPermissionOn;
    }

    public boolean canAccessExternalSd() {
        return (hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));

    }
}
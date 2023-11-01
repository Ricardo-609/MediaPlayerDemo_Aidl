// IMedia.aidl
package com.ricardo.MediaPlayerDemo_Aidl.mediaservice;

// Declare any non-default types here with import statements

interface IMedia {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);


    int getDuration();
    int getCurDuration();
    void setProgress(int pos);
    List<String> getAllSong(String path);
    int getAllSongSize(String sdcard_file);
    void changedSong(int idx);
    void play_pause();
    int getIndex();

}
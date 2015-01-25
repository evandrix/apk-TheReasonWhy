package the.reason.why.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.OnObbStateChangeListener;
import android.os.storage.StorageManager;
import android.util.Log;

public class MediaPlayerActivity
    extends BaseActivity
{
    private static final String TAG_NAME = MediaPlayerActivity.class
                    .getSimpleName();
    private static final String EXP_PATH = "/Android/obb/";

    private static String[] getAPKExpansionFiles(
        Context ctx,
        int mainVersion,
        int patchVersion)
    {
        String packageName = ctx.getPackageName();
        List<String> ret = new ArrayList<String>();
        if (Environment.getExternalStorageState().equals(
            Environment.MEDIA_MOUNTED)) {
            File root = Environment.getExternalStorageDirectory();
            File expPath = new File(root.toString() + EXP_PATH + packageName);
            if (expPath.exists()) {
                if (mainVersion > 0) {
                    String strMainPath = expPath + File.separator + "main."
                                    + mainVersion + "." + packageName + ".obb";
                    File main = new File(strMainPath);
                    if (main.isFile()) {
                        ret.add(strMainPath);
                    }
                }
                if (patchVersion > 0) {
                    String strPatchPath = expPath + File.separator + "patch."
                                    + mainVersion + "." + packageName + ".obb";
                    File main = new File(strPatchPath);
                    if (main.isFile()) {
                        ret.add(strPatchPath);
                    }
                }
            }
        }
        String[] retArray = new String[ret.size()];
        ret.toArray(retArray);
        return retArray;
    }

    private StorageManager mSM;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        for (final String obbFilename : getAPKExpansionFiles(this, 12, 12)) {
            Log.d(TAG_NAME, "obbFilename: " + obbFilename);
            mSM = (StorageManager) getApplicationContext().getSystemService(
                STORAGE_SERVICE);
            OnObbStateChangeListener mEventListener = new OnObbStateChangeListener() {
                @Override
                public void onObbStateChange(String path, int state)
                {
                    Log.d(
                        TAG_NAME,
                        "path="
                                        + path
                                        + "; state="
                                        + (state == OnObbStateChangeListener.MOUNTED ? "mounted"
                                                        : state == OnObbStateChangeListener.UNMOUNTED ? "unmounted"
                                                                        : "error"));
                    if (state == OnObbStateChangeListener.MOUNTED) {
                        Log.d(TAG_NAME, "Mounted! According to the listener");
                        if (mSM.isObbMounted(obbFilename)) {
                            String mountedPath = mSM
                                            .getMountedObbPath(obbFilename);
                            Log.d(TAG_NAME, "mounted @ " + mountedPath);
                            try {
                                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                                audioManager.setStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2,
                                    0);

                                String track = "001.mp3";
                                String filePath = FilenameUtils.concat(
                                    mountedPath, track);
                                File file = new File(filePath);
                                FileInputStream inputStream = new FileInputStream(
                                                file);
                                MediaPlayer mediaPlayer = new MediaPlayer();
                                mediaPlayer.setDataSource(inputStream.getFD());
                                inputStream.close();
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                            } catch (IOException e) {
                                Log.e(TAG_NAME, e.getMessage());
                            }
                        } else {
                            Log.d(TAG_NAME, "NOT mounted");
                        }
                    } else {
                        Log.e(TAG_NAME, "NOT mounted according to the listener");
                    }

                }
            };

            mSM.mountObb(obbFilename, null, mEventListener);
        }

        // ZipResourceFile expansionFile = APKExpansionSupport
        // .getAPKExpansionZipFile(this, 12, 12);
        // Log.d(TAG_NAME, "expansionFile: " + expansionFile);
        // if (expansionFile != null) {
        // AssetFileDescriptor fd = expansionFile
        // .getAssetFileDescriptor(filename);
        // if (fd != null && fd.getFileDescriptor() != null) {
        // Log.d(TAG_NAME,
        // "[" + filename + "] " + "length: " + fd.getLength());
        //
        // Log.d(TAG_NAME, "[" + filename + "] " + "fileDescriptor: "
        // + fd.getFileDescriptor().toString());
        // Log.d(TAG_NAME, "[" + filename + "] " + "file inputStream "
        // + fd.createInputStream().toString());
        // }
        //
        // ZipEntryRO[] ziro = expansionFile.getAllEntries();
        // for (ZipEntryRO entry : ziro) {
        // Log.d(TAG_NAME, "absPath: " + entry.mFile.getAbsolutePath());
        // Log.d(TAG_NAME, "filename: " + entry.mFileName);
        // Log.d(TAG_NAME, "zipFilename: " + entry.mZipFileName);
        // Log.d(TAG_NAME, "compressedLength: "
        // + entry.mCompressedLength);
        // fd = entry.getAssetFileDescriptor();
        // if (fd != null && fd.getFileDescriptor() != null) {
        // Log.d(TAG_NAME, "length: " + fd.getLength());
        //
        // Log.d(TAG_NAME, "fileDescriptor: "
        // + fd.getFileDescriptor().toString());
        // Log.d(TAG_NAME, "file inputStream "
        // + fd.createInputStream().toString());
        //
        // } else {
        // Log.e(TAG_NAME, "fd or fd.getFileDescriptor() is null");
        // }
        // }
        // }
    }
}

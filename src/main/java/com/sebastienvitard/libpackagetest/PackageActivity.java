package com.sebastienvitard.libpackagetest;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentActivity;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PackageActivity extends FragmentActivity
{
    private AppCompatTextView logView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_package);
        bindUI();
    }

    private void bindUI()
    {
        logView = findViewById(R.id.log);
        findViewById(R.id.button_cache).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                runCacheTest();
            }
        });
        findViewById(R.id.button_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                clearLog();
            }
        });
    }

    private void runCacheTest()
    {
        new Thread(new Runnable() {
            public void run()
            {
                createCache();
                writeCache();
                readCache();
            }
        }).start();
    }

    /***********************************************************************************************
     *
     * CACHE
     *
     **********************************************************************************************/

    private static DiskLruCache sCache;
    private static final String CACHE_PATH = "/com.sebastienvitard.libpackagetest/cache";
    private static final int CACHE_SIZE = 5 * 1024 * 1024; // 5 Mo
    private static final int CACHE_VERSION = 1;
    private static final String CACHE_DATA_KEY = "com_sebastienvitard_libpackagetest-data";
    private static final String CACHE_DATA_VALUE = "data";

    private void createCache()
    {
        try
        {
            log("Opening and/or creating cache…");
            String cachePath = getCacheDir().getCanonicalPath() + CACHE_PATH;
            File cacheDir = new File(cachePath);
            sCache = DiskLruCache.open(cacheDir, CACHE_VERSION, 1, CACHE_SIZE);
            log(LOG_STATUS_OK);
        }
        catch (IOException exception)
        {
            log(exception.toString());
        }
    }

    private void writeCache()
    {
        if (sCache != null)
        {
            try
            {
                log("Editing " + CACHE_DATA_KEY + " cache entry…");

                DiskLruCache.Editor editor = sCache.edit(CACHE_DATA_KEY);
                if (editor == null)
                {
                    log("Concurrent access");
                    return;
                }
                ObjectOutputStream out = new ObjectOutputStream(editor.newOutputStream(0));
                out.writeObject(CACHE_DATA_VALUE);
                out.close();
                editor.commit();
                log(LOG_STATUS_OK);
            }
            catch (IOException exception)
            {
                log(exception.toString());
            }
        }
    }

    private void readCache()
    {
        if (sCache != null)
        {
            try
            {
                log("Querying cache for key " + CACHE_DATA_KEY + '…');

                DiskLruCache.Snapshot snapshot = sCache.get(CACHE_DATA_KEY);
                if (snapshot == null)
                {
                    log("No entry found");
                }
                else
                {
                    ObjectInputStream in = new ObjectInputStream(snapshot.getInputStream(0));
                    String data = (String) in.readObject();
                    log("Found " + data);
                    log(CACHE_DATA_VALUE.equals(data) ? LOG_STATUS_OK : "NOK");
                }
            }
            catch (IOException | ClassNotFoundException exception)
            {
                log(exception.toString());
            }
        }
    }

    /***********************************************************************************************
     *
     * LOG
     *
     **********************************************************************************************/

    private static final String LOG_STATUS_OK = "OK";
    private String log = "";

    private void log(@NonNull String entry)
    {
        log += ! log.isEmpty() ? '\n' + entry : entry;

        runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                logView.setText(log);
            }
        });
    }

    private void clearLog()
    {
        log = "";
        logView.setText(log);
    }
}

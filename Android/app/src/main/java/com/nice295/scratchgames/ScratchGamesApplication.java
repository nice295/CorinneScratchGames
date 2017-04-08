package com.nice295.scratchgames;

import android.app.Application;

//import com.tsengvn.typekit.Typekit;

import com.tsengvn.typekit.Typekit;

import io.paperdb.Paper;

/**
 * Created by kyuholee on 2016. 9. 8..
 */
public class ScratchGamesApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Typekit.getInstance()
                .addItalic(Typekit.createFromAsset(this, "scr.ttf"));

        Paper.init(getApplicationContext());
    }
}

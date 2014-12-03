/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.george.sharedelementimplementation;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

/**
 * This example shows how to create a custom activity animation when you want something more
 * than window animations can provide. The idea is to disable window animations for the
 * activities and to instead launch or return from the sub-activity immediately, but use
 * property animations inside the activities to customize the transition.
 *
 * Watch the associated video for this demo on the DevBytes channel of developer.android.com
 * or on the DevBytes playlist in the androiddevelopers channel on YouTube at
 * https://www.youtube.com/playlist?list=PLWz5rJ2EKKc_XOgcRukSoKKjewFJZrKV0.
 */
public class ActivityAnimations extends Activity {

    private static final String TAG = ActivityAnimations.class.getSimpleName();
    private static final String PACKAGE = "com.example.android.activityanim";
    static float sAnimatorScale = 1;

//    GridLayout mGridLayout;
    GridView mGridView;
    PhotoGridAdapter mAdapter;
    ArrayList<PictureData> pictures;
    BitmapUtils mBitmapUtils = new BitmapUtils();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animations);

        // Grayscale filter used on all thumbnails
        ColorMatrix grayMatrix = new ColorMatrix();
        grayMatrix.setSaturation(0);
        ColorMatrixColorFilter grayscaleFilter = new ColorMatrixColorFilter(grayMatrix);

        mGridView = (GridView) findViewById(R.id.gv_photo_grid);
        mAdapter = new PhotoGridAdapter(this);
        Resources resources = getResources();
        pictures = mBitmapUtils.loadPhotos(resources);
        mAdapter.updateData(pictures);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int[] screenLocation = new int[2];
                view.getLocationOnScreen(screenLocation);
                float clipRatio = 1 - (float) pictures.get(position).thumbnail.getHeight() / pictures.get(position).thumbnail.getWidth();
                float ratio = (float) view.getHeight() / pictures.get(position).thumbnail.getHeight();
                screenLocation[0] = (screenLocation[0] + view.getWidth()/2) - (int) (pictures.get(position).thumbnail.getWidth() * ratio)/2;
                Intent subActivity = new Intent(ActivityAnimations.this,
                    PictureDetailsActivity.class);
                int orientation = getResources().getConfiguration().orientation;
                subActivity
                    .putExtra(PACKAGE + ".orientation", orientation)
                    .putExtra(PACKAGE + ".resourceId", pictures.get(position).resourceId)
                    .putExtra(PACKAGE + ".left", screenLocation[0])
                    .putExtra(PACKAGE + ".top", screenLocation[1])
                    .putExtra(PACKAGE + ".width", view.getWidth())
                    .putExtra(PACKAGE + ".height", view.getHeight())
                    .putExtra(PACKAGE + ".description", pictures.get(position).description)
                    .putExtra(PACKAGE + ".clipRatio", clipRatio);
                startActivity(subActivity);

                // Override transitions: we don't want the normal window animation in addition
                // to our custom one
                overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_better_window_animations, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_slow) {
            sAnimatorScale = item.isChecked() ? 1 : 5;
            item.setChecked(!item.isChecked());
        }
        return super.onOptionsItemSelected(item);
    }

}

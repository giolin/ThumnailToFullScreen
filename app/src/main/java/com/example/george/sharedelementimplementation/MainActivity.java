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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.george.sharedelementimplementation.misc.ClickableViewPager;
import com.example.george.sharedelementimplementation.misc.ClippingImageView;
import com.example.george.sharedelementimplementation.misc.MyGridView;
import com.example.george.sharedelementimplementation.misc.PhotoGridAdapter;
import com.example.george.sharedelementimplementation.misc.PictureData;
import com.example.george.sharedelementimplementation.util.BitmapUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final TimeInterpolator sDecelerator = new DecelerateInterpolator();
    private static final TimeInterpolator sAccelerator = new AccelerateInterpolator();

    private static final int ANIM_DURATION = 1500;
    private static float sAnimatorScale = 1;

    RelativeLayout mTopLevelLayout;

    private MyGridView mGridView;
    private ArrayList<PictureData> pictures;
    private BitmapUtils mBitmapUtils = new BitmapUtils();

    private ClippingImageView mImage;
    private ColorDrawable mBackground;
    private ViewPager mPager;

    private int mXDelta;
    private int mYDelta;
    private float mImageScale;
    private float clipRatio;

    private boolean mIsInFullscreen = false;
    private boolean mIsAnimationPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animations);

        mGridView = (MyGridView) findViewById(R.id.gv_photo_grid);
        PhotoGridAdapter mAdapter = new PhotoGridAdapter(this);
        pictures = mBitmapUtils.loadPhotos(getResources());
        mAdapter.updateData(pictures);
        mGridView.setAdapter(mAdapter);

        // the image for pop up animation effect
        mImage = (ClippingImageView) findViewById(R.id.iv_animation);
        mImage.setVisibility(View.GONE);

        // set the background color in the fullscreen
        mTopLevelLayout = (RelativeLayout) findViewById(R.id.rl_fullscreen_bg);
        mBackground = new ColorDrawable(Color.BLACK);
        mBackground.setAlpha(0);
        mTopLevelLayout.setBackground(mBackground);

        mPager = (ClickableViewPager) findViewById(R.id.pager);
        mPager.setVisibility(View.GONE);

        // enable/disable touch event
        mGridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mIsInFullscreen) {
                    // returning true means that this event has been consumed
                    // in fullscreen the grid view is not responding any finger interaction
                    return true;
                }
                return false;
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mIsInFullscreen = true;
                // set the animating image to the clicked item
                Drawable drawable = ((ImageView) view).getDrawable();
                mImage.setImageDrawable(drawable);
                mImage.setVisibility(View.VISIBLE);

                // reset image translation
                mImage.setTranslationX(0);
                mImage.setTranslationY(0);

                // reset pager's adapter every time a view in Grid view is clicked
                mPager.setAdapter(new PhotoViewAdapter(MainActivity.this, pictures));
                mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset,
                        int positionOffsetPixels) {

                    }

                    @Override public void onPageSelected(int position) {
                    }

                    @Override public void onPageScrollStateChanged(int state) {
                        // the GirdView should follow the Pager
                        mGridView.smoothScrollToPosition(mPager.getCurrentItem());
                    }
                });
                mPager.setCurrentItem(position, false);

                final float drawableWidth = drawable.getIntrinsicWidth();
                final float drawableHeight = drawable.getIntrinsicHeight();
                // calculate the clicked view's location and width/height
                final float heightWidthRatio = drawableHeight / drawableWidth;
                final int thumbnailWidth = view.getWidth();
                final int thumbnailHeight = view.getHeight();

                int[] viewLocation = new int[2];
                int[] gridViewLocation = new int[2];
                view.getLocationOnScreen(viewLocation);
                mGridView.getLocationOnScreen(gridViewLocation);
                final int thumbnailX = viewLocation[0] + thumbnailWidth / 2;
                final int thumbnailY = viewLocation[1] + thumbnailHeight / 2;
                final int fullscreenX = gridViewLocation[0] + mGridView.getWidth() / 2;
                final int fullscreenY = gridViewLocation[1] + mGridView.getHeight() / 2;

                Log.d(TAG, "viewLocation=" + viewLocation[0] + ", " + viewLocation[1]
                    + "\ngridViewLocation=" + gridViewLocation[0] + ", " + gridViewLocation[1]);
                Log.d(TAG, "thumbnailX=" + thumbnailX + ", thumbnailY=" + thumbnailY
                    + "\nfullscreenX=" + fullscreenX + ", fullscreenY=" + fullscreenY);

                // for image transform, we need 3 arguments to transform properly:
                // deltaX and deltaY - the translation of the image
                // scale value - the resize value
                // clip ratio - the reveal part of the image

                // figure out where the thumbnail and full size versions are, relative
                // to the screen and each other
                mXDelta = thumbnailX - fullscreenX;
                mYDelta = thumbnailY - fullscreenY;

                // Scale factors to make the large version the same size as the thumbnail
                if (heightWidthRatio < 1) {
                    mImageScale = (float) thumbnailHeight / mImage.getLayoutParams().height;
                } else {
                    mImageScale = (float) thumbnailWidth / mImage.getLayoutParams().width;
                }

                // clip ratio
                if (heightWidthRatio < 1) {
                    // if the original picture is in landscape
                    clipRatio = 1 - heightWidthRatio;
                } else {
                    // if the original picture is in portrait
                    clipRatio = 1 - 1 / heightWidthRatio;
                }

                Log.d(TAG, "*************************Enter Animation*************************");
                Log.d(TAG, "(mXDelta, mTopDelta)=(" + mXDelta + ", " + mYDelta
                    + ")\nmImageScale=" + mImageScale + "\nclipRatio=" + clipRatio);

                runEnterAnimation();
            }
        });
    }

    public void runEnterAnimation() {
        final long duration = (long) (ANIM_DURATION * MainActivity.sAnimatorScale);
        // set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
        ObjectAnimator anim = ObjectAnimator.ofInt(mBackground, "alpha", 0, 255);
        anim.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {

            }

            @Override public void onAnimationEnd(Animator animation) {
                mIsAnimationPlaying = false;
                mImage.setVisibility(View.GONE);
                mPager.setVisibility(View.VISIBLE);
            }

            @Override public void onAnimationCancel(Animator animation) {

            }

            @Override public void onAnimationRepeat(Animator animation) {

            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
            ObjectAnimator.ofFloat(mImage, "translationX", mXDelta, 0),
            ObjectAnimator.ofFloat(mImage, "translationY", mYDelta, 0),
            ObjectAnimator.ofFloat(mImage, "scaleX", mImageScale, 1),
            ObjectAnimator.ofFloat(mImage, "scaleY", mImageScale, 1),
//            ObjectAnimator.ofFloat(mImage, "alpha", 0, 1),
            ObjectAnimator.ofFloat(mImage, "imageCrop", clipRatio, 0f),
            anim
        );
        set.setInterpolator(sDecelerator);
        set.setDuration(duration).start();
        mIsAnimationPlaying = true;
    }

    public void runExitAnimation() {
        final long duration = (long) (ANIM_DURATION * MainActivity.sAnimatorScale);
        ObjectAnimator anim = ObjectAnimator.ofInt(mBackground, "alpha", 0);
        anim.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {
                // do nothing intended
            }

            @Override public void onAnimationEnd(Animator animation) {
                mIsInFullscreen = false;
                mIsAnimationPlaying = false;
                mImage.setVisibility(View.GONE);
            }

            @Override public void onAnimationCancel(Animator animation) {
                // do nothing intended
            }

            @Override public void onAnimationRepeat(Animator animation) {
                // do nothing intended
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
            ObjectAnimator.ofFloat(mImage, "translationX", 0, mXDelta),
            ObjectAnimator.ofFloat(mImage, "translationY", 0, mYDelta),
            ObjectAnimator.ofFloat(mImage, "scaleX", 1, mImageScale),
            ObjectAnimator.ofFloat(mImage, "scaleY", 1, mImageScale),
//            ObjectAnimator.ofFloat(mImage, "alpha", 1, 0),
            ObjectAnimator.ofFloat(mImage, "imageCrop", 0f, clipRatio),
            anim
        );
        set.setInterpolator(sAccelerator);
        set.setDuration(duration).start();
        mPager.setVisibility(View.GONE);
        mIsAnimationPlaying = true;
    }

    @Override public void onBackPressed() {
        if (mIsInFullscreen) {
            if (!mIsAnimationPlaying) {

                View view = mGridView.getViewByPosition(mPager.getCurrentItem());

                Drawable drawable = ((ImageView) view).getDrawable();

                mImage.setImageDrawable(drawable);
                mImage.setVisibility(View.VISIBLE);

                final float drawableWidth = drawable.getIntrinsicWidth();
                final float drawableHeight = drawable.getIntrinsicHeight();
                final float heightWidthRatio = drawableHeight / drawableWidth;
                final int thumbnailWidth = view.getWidth();
                final int thumbnailHeight = view.getHeight();

                int[] viewLocation = new int[2];
                int[] gridViewLocation = new int[2];
                view.getLocationOnScreen(viewLocation);
                mGridView.getLocationOnScreen(gridViewLocation);
                final int thumbnailX = viewLocation[0] + view.getWidth() / 2;
                final int thumbnailY = viewLocation[1] + view.getHeight() / 2;
                final int fullscreenX = gridViewLocation[0] + mGridView.getWidth() / 2;
                final int fullscreenY = gridViewLocation[1] + mGridView.getHeight() / 2;
                Log.d(TAG, "viewLocation=" + viewLocation[0] + ", " +
                    "" + viewLocation[1] + "\ngridViewLocation=" + gridViewLocation[0] + ", " +
                    "" + gridViewLocation[1]);
                Log.d(TAG, "thumbnailX=" + thumbnailX + ", thumbnailY=" + thumbnailY +
                    "\nfullscreenX=" + fullscreenX + ", fullscreenY=" + fullscreenY);
                Log.d(TAG, "(thumbnailWidth, thumbnailHeight)=(" + thumbnailWidth + ", " +
                    "" + thumbnailHeight + ")");
                Log.d(TAG, "drawableHeight=" + drawableHeight + "\ndrawableWidth=" +
                    drawableWidth + "\nheightWidthRatio=" + heightWidthRatio);

                // Figure out where the thumbnail and full size versions are, relative
                // to the screen and each other
                mXDelta = thumbnailX - fullscreenX;
                mYDelta = thumbnailY - fullscreenY;

                // Scale factors to make the large version
                // the same size as the thumbnail
                if (heightWidthRatio < 1) {
                    mImageScale = (float) thumbnailHeight / mImage.getLayoutParams().height;
                } else {
                    mImageScale = (float) thumbnailWidth / mImage.getLayoutParams().width;
                }

                if (heightWidthRatio < 1) {
                    // is the original picture is in landscape
                    clipRatio = 1 - heightWidthRatio;
                } else {
                    // if the original picture in portrait
                    clipRatio = 1 - 1 / heightWidthRatio;
                }

                Log.d(TAG, "*************************Exit Animation*************************");
                Log.d(TAG, "(mXDelta, mTopDelta)=(" + mXDelta + ", " + mYDelta
                    + ")\nmImageScale=" + mImageScale
                    + "\nclipRatio=" + clipRatio);

                // reset image translation
                mImage.setTranslationX(0);
                mImage.setTranslationY(0);

                runExitAnimation();
            }
        } else {
            super.onBackPressed();
        }
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

    static class PhotoViewAdapter extends PagerAdapter {

        private Activity mActivity;
        private List<PictureData> mPictureDatas;

        PhotoViewAdapter(Activity activity, List<PictureData> pictureDatas) {
            this.mActivity = activity;
            this.mPictureDatas = pictureDatas;
        }

        @Override
        public int getCount() {
            return mPictureDatas.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float v, float v2) {
//                    if (mActivity.getActionBar().isShowing()) {
//                        mActivity.getActionBar().hide();
//                    } else {
//                        mActivity.getActionBar().show();
//                    }
                }
            });
//            String url = PhotoPlatformContract.ExternalThumbnails.uri(mImageDocKeys.get(position)
//                , 2).toString();
            Picasso.with(mActivity)
                .load(mPictureDatas.get(position).getResourceId())
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.holo_red_light)
                .into(photoView);
            container.addView(photoView, MATCH_PARENT, MATCH_PARENT);
            // Set the tag of the view for the pager to find view with tag
            photoView.setTag(position);
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

}

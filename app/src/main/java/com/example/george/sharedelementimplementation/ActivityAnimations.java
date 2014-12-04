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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * This example shows how to create a custom activity animation when you want something more
 * than window animations can provide. The idea is to disable window animations for the
 * activities and to instead launch or return from the sub-activity immediately, but use
 * property animations inside the activities to customize the transition.
 * <p/>
 * Watch the associated video for this demo on the DevBytes channel of developer.android.com
 * or on the DevBytes playlist in the androiddevelopers channel on YouTube at
 * https://www.youtube.com/playlist?list=PLWz5rJ2EKKc_XOgcRukSoKKjewFJZrKV0.
 */
public class ActivityAnimations extends ActionBarActivity {

    private static final String TAG = ActivityAnimations.class.getSimpleName();
    private static final TimeInterpolator sDecelerator = new DecelerateInterpolator();
    private static final TimeInterpolator sAccelerator = new AccelerateInterpolator();
    private static final int ANIM_DURATION = 300;
    static float sAnimatorScale = 1;

    private MyGridView mGridView;
    private PhotoGridAdapter mAdapter;
    private ArrayList<PictureData> pictures;
    private BitmapUtils mBitmapUtils = new BitmapUtils();

    private ClippingImageView mImage;
    //    private PhotoView mImage;
    private ColorDrawable mBackground;
    private RelativeLayout mTopLevelLayout;
    private ViewPager mPager;

    private int mLeftDelta;
    private int mTopDelta;
    private float mImageScale;
    private float clipRatio;

    private boolean mIsInFullscreen = false;
    private boolean mIsAnimationPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animations);

        mImage = (ClippingImageView) findViewById(R.id.iv_full_size);
        mImage.setVisibility(View.GONE);

        // set the background color in the fullscreen
        mTopLevelLayout = (RelativeLayout) findViewById(R.id.rl_fullscreen_bg);
        mBackground = new ColorDrawable(Color.BLACK);
        mBackground.setAlpha(0);
        mTopLevelLayout.setBackground(mBackground);

        mGridView = (MyGridView) findViewById(R.id.gv_photo_grid);
        mAdapter = new PhotoGridAdapter(this);
        Resources resources = getResources();
        pictures = mBitmapUtils.loadPhotos(resources);
        mAdapter.updateData(pictures);
        mGridView.setAdapter(mAdapter);

        mPager = (HackyViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new PhotoViewAdapter(this, pictures));
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

                Bitmap bitmap = BitmapUtils.getBitmap(getResources(),
                    pictures.get(position).resourceId);
                Drawable mBitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                mImage.setImageDrawable(mBitmapDrawable);

                int[] viewLocation = new int[2];
                view.getLocationOnScreen(viewLocation);

                final float heightWidthRatio = (float) bitmap.getHeight() / bitmap.getWidth();
                Log.d(TAG, "bitmap.getHeight()=" + bitmap.getHeight() + "\nbitmap.getWidth()=" +
                    bitmap.getWidth() + "\nheightWidthRatio=" + heightWidthRatio);

                if (heightWidthRatio < 1) {
                    // is the original picture is in landscape
                    clipRatio = 1 - heightWidthRatio;
                    viewLocation[0] = (int) (viewLocation[0] + view.getWidth() / 2
                        - view.getHeight() / (2 * heightWidthRatio));
                } else {
                    // if the original picture in portrait
                    clipRatio = 1 - 1 / heightWidthRatio;
                    viewLocation[1] = (int) (viewLocation[1] + view.getHeight() / 2
                        - view.getWidth() * heightWidthRatio / 2);
                }

                final int thumbnailLeft = viewLocation[0];
                final int thumbnailTop = viewLocation[1];
                final int thumbnailWidth = view.getWidth();
                final int thumbnailHeight = view.getHeight();

                Log.d(TAG, "(thumbnailLeft, thumbnailTop)=(" + thumbnailLeft + ", " +
                    "" + thumbnailTop + ")\n"
                    + "(thumbnailWidth, thumbnailHeight)=(" + thumbnailWidth + ", " +
                    "" + thumbnailHeight + ")");

                mImage.setVisibility(View.VISIBLE);
                // reset pager's adapter every time a view in Grid view is clicked
                mPager.setAdapter(new PhotoViewAdapter(ActivityAnimations.this, pictures));
                mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset,
                        int positionOffsetPixels) {

                    }

                    @Override public void onPageSelected(int position) {
                        Log.d(TAG, "onPageSelected");
                    }

                    @Override public void onPageScrollStateChanged(int state) {
                        Log.d(TAG, "onPageScrollStateChanged");
                        mGridView.smoothScrollToPosition(mPager.getCurrentItem());
                    }
                });
                mPager.setCurrentItem(position, false);
                ViewTreeObserver observer = mImage.getViewTreeObserver();
                observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mImage.getViewTreeObserver().removeOnPreDrawListener(this);
                        // reset image pivot to scale normally
                        mImage.setPivotX(0);
                        mImage.setPivotY(0);
                        // reset image translation
                        mImage.setTranslationX(0);
                        mImage.setTranslationY(0);

                        // Figure out where the thumbnail and full size versions are, relative
                        // to the screen and each other
                        int[] imageFullscreenLocation = new int[2];
                        mImage.getLocationOnScreen(imageFullscreenLocation);
                        mLeftDelta = thumbnailLeft - imageFullscreenLocation[0];
                        mTopDelta = thumbnailTop - imageFullscreenLocation[1];
                        Log.d(TAG, "(imageFinalLeft, " +
                            "imageFianlTop)=(" + imageFullscreenLocation[0] + ", " +
                            "" + imageFullscreenLocation[1] + ")");

                        // Scale factors to make the large version the same size as the thumbnail
                        if (heightWidthRatio < 1) {
                            mImageScale = (float) thumbnailHeight / mImage.getHeight();
                        } else {
                            mImageScale = (float) thumbnailWidth / mImage.getWidth();
                        }
                        runEnterAnimation();
                        return true;
                    }
                });

            }
        });
    }

    public void runEnterAnimation() {
        final long duration = (long) (ANIM_DURATION * ActivityAnimations.sAnimatorScale);
        // set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
        Log.d(TAG, "(mLeftDelta, mTopDelta)=(" + mLeftDelta + ", " + mTopDelta
            + ")\nmImageScale=" + mImageScale
            + "\nclipRatio=" + clipRatio);
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
            ObjectAnimator.ofFloat(mImage, "translationX", mLeftDelta, 0),
            ObjectAnimator.ofFloat(mImage, "translationY", mTopDelta, 0),
            ObjectAnimator.ofFloat(mImage, "scaleX", mImageScale, 1),
            ObjectAnimator.ofFloat(mImage, "scaleY", mImageScale, 1),
            ObjectAnimator.ofFloat(mImage, "alpha", 0, 1),
            ObjectAnimator.ofFloat(mImage, "imageCrop", clipRatio, 0f),
            anim
        );
        set.setInterpolator(sDecelerator);
        set.setDuration(duration).start();
        mIsAnimationPlaying = true;
    }

    public void runExitAnimation() {
        final long duration = (long) (ANIM_DURATION * ActivityAnimations.sAnimatorScale);
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
            ObjectAnimator.ofFloat(mImage, "translationX", 0, mLeftDelta),
            ObjectAnimator.ofFloat(mImage, "translationY", 0, mTopDelta),
            ObjectAnimator.ofFloat(mImage, "scaleX", 1, mImageScale),
            ObjectAnimator.ofFloat(mImage, "scaleY", 1, mImageScale),
            ObjectAnimator.ofFloat(mImage, "alpha", 1, 0),
            ObjectAnimator.ofFloat(mImage, "imageCrop", 0f, clipRatio),
            anim
        );
        set.setInterpolator(sAccelerator);
        set.setDuration(duration).start();
        mImage.setVisibility(View.VISIBLE);
        mPager.setVisibility(View.GONE);
        mIsAnimationPlaying = true;
    }

    @Override public void onBackPressed() {
        if (mIsInFullscreen) {
            if (!mIsAnimationPlaying) {

                Bitmap bitmap = BitmapUtils.getBitmap(getResources(),
                    pictures.get(mPager.getCurrentItem()).resourceId);

                Drawable mBitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                mImage.setImageDrawable(mBitmapDrawable);

                View view = mGridView.getViewByPosition(mPager.getCurrentItem());
                int[] viewLocation = new int[2];
                view.getLocationOnScreen(viewLocation);

                final float heightWidthRatio = (float) bitmap.getHeight() / bitmap.getWidth();

                if (heightWidthRatio < 1) {
                    // is the original picture is in landscape
                    clipRatio = 1 - heightWidthRatio;
                    viewLocation[0] = (int) (viewLocation[0] + view.getWidth() / 2
                        - view.getHeight() / (2 * heightWidthRatio));
                } else {
                    // if the original picture in portrait
                    clipRatio = 1 - 1 / heightWidthRatio;
                    viewLocation[1] = (int) (viewLocation[1] + view.getHeight() / 2
                        - view.getWidth() * heightWidthRatio / 2);
                }
                final int thumbnailLeft = viewLocation[0];
                final int thumbnailTop = viewLocation[1];
                final int thumbnailWidth = view.getWidth();
                final int thumbnailHeight = view.getHeight();

                Log.d(TAG, "(thumbnailLeft, thumbnailTop)=(" + thumbnailLeft + ", " +
                    "" + thumbnailTop + ")\n"
                    + "(thumbnailWidth, thumbnailHeight)=(" + thumbnailWidth + ", " +
                    "" + thumbnailHeight + ")");

                // reset image pivot to scale normally
                mImage.setPivotX(0);
                mImage.setPivotY(0);
                // reset image translation
                mImage.setTranslationX(0);
                mImage.setTranslationY(0);

                // Figure out where the thumbnail and full size versions are, relative
                // to the screen and each other
                int[] imageFullscreenLocation = new int[2];
                mImage.getLocationOnScreen(imageFullscreenLocation);
                mLeftDelta = thumbnailLeft - imageFullscreenLocation[0];
                mTopDelta = thumbnailTop - imageFullscreenLocation[1];
                Log.d(TAG, "(imageFinalLeft, " +
                    "imageFianlTop)=(" + imageFullscreenLocation[0] + ", " +
                    "" + imageFullscreenLocation[1] + ")");

                // Scale factors to make the large version the same size as the thumbnail
                if (heightWidthRatio < 1) {
                    mImageScale = (float) thumbnailHeight / mImage.getHeight();
                } else {
                    mImageScale = (float) thumbnailWidth / mImage.getWidth();
                }
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
                .load(mPictureDatas.get(position).resourceId)
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

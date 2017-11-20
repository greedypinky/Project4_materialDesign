package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */

/**
 * TODO:
 * Currently, your app doesn't use Snackbars. So please use it at least once in your app. Here is a simple tutorial that you can check out.
 * Hint: you could show it when the app is done loading the article data or if there is no Internet connection.
 */

/**
 * TODO:
 * To make our app even more amazing, we can try animating the title, byline and body text
 * (parallax effect; fade in/out animation, via the View.setAlpha) API)
 * in the details screen, using ViewPager.PageTransformer. You can learn more here.
 */

/**
 * To make our app even more amazing, we can try animating the title, byline and body text (parallax effect; fade in/out animation, via the View.setAlpha) API) in the details screen,
 *  using ViewPager.PageTransformer. You can learn more here.
 *  https://medium.com/@BashaChris/the-android-viewpager-has-become-a-fairly-popular-component-among-android-apps-its-simple-6bca403b16d4
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, ArticleDetailFragment.SetCallBack {

    private static final String TAG = ArticleDetailActivity.class.getSimpleName();
    private Cursor mCursor;
    private long mStartId;

    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int mTopInset;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private View mUpButtonContainer;
    private View mUpButton;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolBar;
    private ImageView mToolBarImage;
    private FloatingActionButton mShareFAB;
    private String mImageURL;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);
        // TODO: get the ImageView for ToolBar
        mToolBarImage = (ImageView) findViewById(R.id.detail_image);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.detail_collapsingtoolbar_layout);
        // Do we need to set the title
        // collapsingToolbarLayout.setTitle(getResources().getString(R.string.user_name));
        mToolBar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        getLoaderManager().initLoader(0, null, this);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                // TODO: remove the following to avoid NullPointerException !
                // java.lang.NullPointerException: Attempt to invoke virtual method 'android.view.ViewPropertyAnimator android.view.View.animate()' on a null object reference
//                mUpButton.animate()
//                        .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
//                        .setDuration(300);
            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);

                    Log.d("onPageSelected", "this is called when page is selected: " + position);

                    loadImage();
                    setBitMapForToolBar(mBitmap);
                }
                mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
                updateUpButtonPosition();
            }
        });

       // mUpButtonContainer = findViewById(R.id.up_container);

       // mUpButton = findViewById(R.id.action_up);
//        mUpButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onSupportNavigateUp();
//            }
//        });

        // TODO: remove this part ?

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mUpButtonContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
//                @Override
//                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
//                    view.onApplyWindowInsets(windowInsets);
//                    mTopInset = windowInsets.getSystemWindowInsetTop();
//                    mUpButtonContainer.setTranslationY(mTopInset);
//                    updateUpButtonPosition();
//                    return windowInsets;
//                }
//            });
//        }

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;

                Log.d(TAG, "onCreated-what is the mSelectedItemId?" + mSelectedItemId);

            }
        }

        mShareFAB = (FloatingActionButton) findViewById(R.id.share_fab);
        mShareFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArticleDetailFragment fragment = getCurrentFragmentFromPager();
                String sharedContent = fragment.getSharedContent();

                // check the network connectivity, if there is no network,show the Snackbar instead of starting share activity
                if (!checkNetworkConnectivity()) {

                    Snackbar.make(mShareFAB.getRootView(), "No network!", Snackbar.LENGTH_LONG).show();

                } else {
                    startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(ArticleDetailActivity.this)
                            .setType("text/plain")
                            .setChooserTitle(getString(R.string.share_article))
                            .setText(sharedContent)
                            .getIntent(), getString(R.string.action_share)));
                }
            }
        });

    }


    /**
     * getCurrentFragmentFromPager
     * Get the fragment from ViewPager to get the article content
     * @return
     */
    private ArticleDetailFragment getCurrentFragmentFromPager() {
         if (mPager != null) {
             int index = mPager.getCurrentItem();
             Log.d(TAG, "getCurrentFragmentFromPager mPager current item index:" + index);
             MyPagerAdapter adapter = (MyPagerAdapter) mPager.getAdapter();

             ArticleDetailFragment fragment = (ArticleDetailFragment) adapter.instantiateItem(mPager,index);
             return fragment;
         } else {
             return null;
         }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    Log.d(TAG, "If mStartId > 0 Page's current item:" + position);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;

            // Does not really work
//            if (mCursor != null) {
//                if(!mCursor.isLast()) {
//                    mCursor.moveToFirst();
//                }

                loadImage();
                setBitMapForToolBar(mBitmap);
            //}
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    public void onUpButtonFloorChanged(long itemId, ArticleDetailFragment fragment) {
        if (itemId == mSelectedItemId) {
            mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
            updateUpButtonPosition();
        }
    }

    private void updateUpButtonPosition() {

        // TODO: Comment out - call this method but do nothing!
        /*
        int upButtonNormalBottom = mTopInset + mUpButton.getHeight();
        mUpButton.setTranslationY(Math.min(mSelectedItemUpButtonFloor - upButtonNormalBottom, 0));
        */
    }


    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            ArticleDetailFragment fragment = (ArticleDetailFragment) object;
            if (fragment != null) {
                mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
                updateUpButtonPosition();
            }
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }


    // TODO: implement the callback - set the bitmap for the ToolBar but it did not work
    @Override
    public void setToolBarBitMap(Bitmap bitmap) {
        // TODO: add implementation
        mToolBarImage.setImageBitmap(bitmap);
        Log.d(TAG,"setToolBarBitMap");

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {

                Palette.Swatch vibrant = palette.getVibrantSwatch();
                if (vibrant != null) {
                    mCollapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(vibrant.getRgb()));
                }
            }
        });
    }

    /**
     * setBitMapForToolBar
     * Set the Article bitmap image to the toolbar
     * also update the ContentScrimColor by the image vibrant color
     * @param bitmap
     */
    public void setBitMapForToolBar(Bitmap bitmap) {
        // TODO: add implementation
        mToolBarImage.setImageBitmap(bitmap);
        Log.d(TAG,"setToolBarBitMap");

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch vibrant = palette.getVibrantSwatch();
                if (vibrant != null) {
                    mCollapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(vibrant.getRgb()));
                }
            }
        });
    }

    /**
     * loadImage
     * Load image from the PhotoURL
     */
    private void loadImage() {
        Bitmap bitmap = null;
        ImageLoaderHelper.getInstance(this).getImageLoader()
                .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                        mBitmap = imageContainer.getBitmap();
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });

    }

    /**
     * checkNetworkConnectivity
     * @return
     */
    private boolean checkNetworkConnectivity() {
        // https://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
        ConnectivityManager connectivityManager = (ConnectivityManager)this.getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if(activeNetwork != null) {
            activeNetwork.isConnectedOrConnecting();
            return true;
        } else {
            return false;
        }
    }

}

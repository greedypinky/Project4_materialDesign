package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
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
public class ArticleDetailActivity extends ActionBarActivity
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
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(ArticleDetailActivity.this)
                        .setType("text/plain")
                        .setChooserTitle(getString(R.string.share_article))
                        .setText(sharedContent)
                        .getIntent(), getString(R.string.action_share)));
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
             //ArticleDetailFragment fragment = (ArticleDetailFragment) adapter.getItem(index);
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


    // TODO: implement the callback - set the bitmap for the ToolBar
    @Override
    public void setToolBarBitMap(Bitmap bitmap) {
        // TODO: add implementation
        mToolBarImage.setImageBitmap(bitmap);
        Log.d(TAG,"setToolBarBitMap");

        // Set the Toolbar color dynamically based on the Bitmap Palette color
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
//                R.drawable.profile_pic);
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                //mCollapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(R.attr.colorPrimary));
                //mCollapsingToolbarLayout.setStatusBarScrimColor(palette.getMutedColor(R.attr.colorPrimaryDark);

                Palette.Swatch vibrant = palette.getVibrantSwatch();
                if (vibrant != null) {
                    // Set the background color of a layout based on the vibrant color
                    // containerView.setBackgroundColor(vibrant.getRgb());
                    // Update the title TextView with the proper text color
                    // titleView.setTextColor(vibrant.getTitleTextColor());
                    mCollapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(vibrant.getRgb()));
                }
            }
        });
    }

    public void setBitMapForToolBar(Bitmap bitmap) {
        // TODO: add implementation
        mToolBarImage.setImageBitmap(bitmap);
        Log.d(TAG,"setToolBarBitMap");

        // Set the Toolbar color dynamically based on the Bitmap Palette color
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
//                R.drawable.profile_pic);
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                //mCollapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(R.attr.colorPrimary));
                //mCollapsingToolbarLayout.setStatusBarScrimColor(palette.getMutedColor(R.attr.colorPrimaryDark);

                Palette.Swatch vibrant = palette.getVibrantSwatch();
                if (vibrant != null) {
                    // Set the background color of a layout based on the vibrant color
                    // containerView.setBackgroundColor(vibrant.getRgb());
                    // Update the title TextView with the proper text color
                    // titleView.setTextColor(vibrant.getTitleTextColor());
                    mCollapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(vibrant.getRgb()));
                }
            }
        });
    }

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

}

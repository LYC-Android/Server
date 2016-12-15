package mrcheng.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import util.BaseActivity;
import util.FragmentAdapter;
import util.HaveDownloadFragment;
import util.NoDownloadFragment;

/**
 * Created by mr.cheng on 2016/9/10.
 */
public class DownloadActivity extends BaseActivity {
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.tabs)
    TabLayout mTabs;
    @InjectView(R.id.viewpager)
    ViewPager mViewpager;
    @InjectView(R.id.appbar)
    AppBarLayout mAppbar;
    @InjectView(R.id.download)
    FloatingActionButton mDownload;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_download);
        //记得要把这玩意给注释掉了
        ButterKnife.inject(this);
        ShowEnterAnimation();
        setTitle("离线下载");
        setSupportActionBar(mToolbar);
        android.support.v7.app.ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        initViewPager();
    }

    private void ShowEnterAnimation() {
        Transition transition = getWindow().getSharedElementEnterTransition();
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                mAppbar.setVisibility(View.GONE);
                mViewpager.setVisibility(View.GONE);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
                animateRevealShow();
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
    }

    private void animateRevealShow() {
        Animator mAnimator = ViewAnimationUtils.createCircularReveal(mAppbar,
                mAppbar.getWidth() / 2, mAppbar.getHeight() / 2,
                mAppbar.getHeight() / 2, mAppbar.getHeight());
        mAnimator.setDuration(200);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAppbar.setVisibility(View.VISIBLE);
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mViewpager.setVisibility(View.VISIBLE);
                mDownload.setVisibility(View.INVISIBLE);
                super.onAnimationEnd(animation);
            }
        });
        mAnimator.start();
    }

    private void initViewPager() {
        List<String> titles = new ArrayList<>();
        titles.add("未下载");
        titles.add("已下载");
        mTabs.addTab(mTabs.newTab().setText(titles.get(0)));
        mTabs.addTab(mTabs.newTab().setText(titles.get(1)));
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new NoDownloadFragment());
        fragments.add(new HaveDownloadFragment());
        FragmentAdapter adapter =
                new FragmentAdapter(getSupportFragmentManager(), fragments, titles);
        mViewpager.setAdapter(adapter);
        mTabs.setupWithViewPager(mViewpager);
        mTabs.setTabsFromPagerAdapter(adapter);
        mTabs.setTabMode(TabLayout.MODE_FIXED);

    }

    @Override
    public void onBackPressed() {
        mDownload.setVisibility(View.VISIBLE);
        animateRevealClose();
    }

    private void animateRevealClose() {

        Animator mAnimator = ViewAnimationUtils.createCircularReveal(mAppbar,
                mAppbar.getWidth()/2,mAppbar.getHeight() / 2,
                mAppbar.getWidth(),mDownload.getWidth()/2);
        mAnimator.setDuration(200);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAppbar.setVisibility(View.INVISIBLE);
                super.onAnimationEnd(animation);
                DownloadActivity.super.onBackPressed();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

            }
        });
        mAnimator.start();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }
}
package com.example.partymaker.ui.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.example.partymaker.R;

public class IntroActivity extends Activity {

  private ViewPager viewPager;
  private LinearLayout dotsLayout;
  private int[] layouts;
  private Button btnSkip, btnNext;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // set as fullscreen
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow()
        .setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    setContentView(R.layout.activity_intro);

    viewPager = findViewById(R.id.view_pager);
    dotsLayout = findViewById(R.id.layoutDots);
    btnSkip = findViewById(R.id.btn_skip);
    btnNext = findViewById(R.id.btn_next);

    layouts =
        new int[] {
          R.layout.activity_intro_slider1,
          R.layout.activity_intro_slider2,
          R.layout.activity_intro_slider3
        };

    // adding bottom dots
    addBottomDots(0);

    ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter();
    viewPager.setAdapter(viewPagerAdapter);
    viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
  }

  public void btnSkipClick(View v) {
    launchHomeScreen();
  }

  public void btnNextClick(View v) {
    // checking for last page
    // if last page home screen will be launched
    int current = getItem(1);
    if (current < layouts.length) {
      // move to next screen
      viewPager.setCurrentItem(current);
    } else {
      launchHomeScreen();
    }
  }

  ViewPager.OnPageChangeListener viewPagerPageChangeListener =
      new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
          addBottomDots(position);

          // changing the next button text 'NEXT' / 'GOT IT'
          if (position == layouts.length - 1) {
            // last page. make button text to GOT IT
            btnNext.setText(getString(R.string.start));
            btnSkip.setVisibility(View.GONE);
          } else {
            // still pages are left
            btnNext.setText(getString(R.string.next));
            btnSkip.setVisibility(View.VISIBLE);
          }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {}

        @Override
        public void onPageScrollStateChanged(int arg0) {}
      };

  private void addBottomDots(int currentPage) {
    TextView[] dots = new TextView[layouts.length];

    dotsLayout.removeAllViews();
    for (int i = 0; i < dots.length; i++) {
      dots[i] = new TextView(this);
      dots[i].setText(Html.fromHtml("&#8226;"));
      dots[i].setTextSize(35);
      dots[i].setTextColor(getResources().getColor(R.color.dot_inactive));
      dotsLayout.addView(dots[i]);
    }

    if (dots.length > 0)
      dots[currentPage].setTextColor(getResources().getColor(R.color.dot_active));
  }

  private int getItem(int i) {
    return viewPager.getCurrentItem() + i;
  }

  private void launchHomeScreen() {
    startActivity(new Intent(getBaseContext(), LoginActivity.class));
    finish();
  }

  public class ViewPagerAdapter extends PagerAdapter {

    public ViewPagerAdapter() {}

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
      LayoutInflater layoutInflater =
          (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

      View view = layoutInflater.inflate(layouts[position], container, false);
      container.addView(view);

      return view;
    }

    @Override
    public int getCount() {
      return layouts.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
      return view == obj;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
      View view = (View) object;
      container.removeView(view);
    }
  }
}

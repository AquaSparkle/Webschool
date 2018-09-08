package gescis.webschool.Fragment;

import android.app.Fragment;
import android.icu.text.RelativeDateTimeFormatter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import gescis.webschool.Adapter.CustomPagerAdapter;
import gescis.webschool.Employactivity;
import gescis.webschool.R;
import gescis.webschool.Studentactivity;
import gescis.webschool.Wschool;
import gescis.webschool.utils.Volley_load;

/**
 * Created by shalu on 10/07/17.
 */

public class Newsfeed_detail extends Fragment
{
    View view;
    TextView title, descrp, nf_date;
    ImageView news_img;
    String newsid;
    ViewPager viewPager;
    ImageView[] dots;
    LinearLayout indicator;
    RelativeLayout imageLayout;
    Timer timer;
    final long DELAY_MS = 500; //delay in milliseconds before task is to be executed
    final long PERIOD_MS = 3000;
    NestedScrollView scrollView;
    int currentPage = 0, pageCount = 3;
    ArrayList<String> imageList, imageNameList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.newsfeed_details, container, false);
        title = (TextView) view.findViewById(R.id.title);
        descrp = (TextView) view.findViewById(R.id.descrp);
        nf_date = (TextView) view.findViewById(R.id.nf_date);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        indicator = (LinearLayout) view.findViewById(R.id.indicator);
        imageLayout = (RelativeLayout) view.findViewById(R.id.image_layout);
        scrollView = (NestedScrollView) view.findViewById(R.id.nested);
        imageList = new ArrayList<>();
        imageNameList = new ArrayList<>();
        if(getArguments()!=null) {
            newsid = getArguments().getString("newsid");
            showDetails();
        }

        if(Wschool.employee_log){
            ((Employactivity) getActivity()).title.setText("News Feeds");
        }else{
            ((Studentactivity) getActivity()).title.setText("News Feeds");
        }
        return view;
    }

    private void showDetails()
    {
        final boolean[] hasFeedImage = {false};
        Map<String,String> params = new HashMap<String, String>();
        params.put("username", Wschool.sharedPreferences.getString("userid", "0"));
        params.put("newsfeedsid", newsid);
        if(Wschool.sharedPreferences.getString("login", "0").equals("guardian"))
        {
            params.put("studentid", Wschool.sharedPreferences.getString("studentid", "0"));
        }
        String url = "newsfeedsdetails";

        new Volley_load(getActivity(), Newsfeed_detail.this, url, params, new Volley_load.Contents() {
            @Override
            public void returndata(JSONArray s)
            {
                try
                {
                   JSONObject jo = s.getJSONObject(0);
                    title.setText(jo.getString("newsfeeds_title"));
                    String nfdate = jo.getString("newsfeeds_date");
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                        descrp.setText(Html.fromHtml(jo.getString("newsfeeds_description"), Html.FROM_HTML_MODE_COMPACT));
                    }else{
                        descrp.setText(Html.fromHtml(jo.getString("newsfeeds_description")));
                    }
                    pageCount = jo.getInt("attachment_count");
                    if(pageCount > 0){
                        imageLayout.setVisibility(View.VISIBLE);
                        String newsfeedsImage = jo.getString("newsfeeds_image");
                        if(!newsfeedsImage.equals("NIL")){
                            imageList.add(jo.getString("newsfeeds_image"));
                            imageNameList.add(jo.getString("filename"));
                            hasFeedImage[0] = true;
                        }
                        for (int i = 1; i <= pageCount; i++) {
                            imageList.add(jo.getString("attachment"+i+"url"));
                            imageNameList.add(jo.getString("attachment"+i));
                        }
                        if(hasFeedImage[0]){
                            pageCount++;
                        }
                        dots = new ImageView[pageCount];
                        viewPager.setAdapter(new CustomPagerAdapter(getActivity(), imageList, imageNameList));
                        pagerController();
                        autoScroll();

                        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                            @Override
                            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                            }

                            @Override
                            public void onPageSelected(int position) {

                                if (!isAdded())
                                    return;

                                for (int i = 0; i < pageCount; i++) {
                                    dots[i].setImageDrawable(getResources().getDrawable(R.drawable.ic_banner_indicator));
                                }
                                dots[position].setImageDrawable(getResources().getDrawable(R.drawable.ic_banner_indicator_checked));
                            }

                            @Override
                            public void onPageScrollStateChanged(int state) {

                            }
                        });
                    }else{
                        imageLayout.setVisibility(View.GONE);
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdf.parse(nfdate);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy");
                    String formattedDate = outputFormat.format(date);

                    nf_date.setText(formattedDate);
                }catch(Exception e)
                {
                    e.printStackTrace();
                }

            }
        });

    }

    private void autoScroll() {
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == pageCount) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
            }
        };

        timer = new Timer(); // This will create a new Thread
        timer .schedule(new TimerTask() { // task to be scheduled

            @Override
            public void run() {
                handler.post(Update);
            }
        }, DELAY_MS, PERIOD_MS);
    }

    private void pagerController() {
        for (int i = 0; i < pageCount; i++) {

            final int pos = i;
            dots[i] = new ImageView(getActivity());
            dots[i].setId(i);
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.ic_banner_indicator));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(6, 0, 6, 0);

            dots[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewPager.setCurrentItem(pos);
                    currentPage = pos;
                    for (ImageView image : dots) {
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_banner_indicator));
                    }
                    dots[pos].setImageDrawable(getResources().getDrawable(R.drawable.ic_banner_indicator_checked));

                }
            });
            indicator.addView(dots[i], params);
        }

        dots[0].setImageDrawable(getResources().getDrawable(R.drawable.ic_banner_indicator_checked));
    }
}

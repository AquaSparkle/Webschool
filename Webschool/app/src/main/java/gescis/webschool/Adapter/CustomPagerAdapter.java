package gescis.webschool.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

import gescis.webschool.ImageActivity;
import gescis.webschool.R;

/**
 * Created by Shalu on 14-09-2017.
 */

public class CustomPagerAdapter extends PagerAdapter {

    private Context mContext;
    ArrayList<String> dataArray, fileNameList;
    int count;
    ImageView banner;

    public CustomPagerAdapter(Context context, ArrayList<String> data, ArrayList<String> imageNameList) {
        mContext = context;
        dataArray = data;
        fileNameList = imageNameList;
        count = dataArray.size();
    }

    @Override
    public Object instantiateItem(ViewGroup collection, final int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.viewpagechild, collection, false);
        collection.addView(layout);

        banner = (ImageView) layout.findViewById(R.id.sliderimg);
        final String imageUrl = dataArray.get(position);
        Picasso.with(mContext).load(imageUrl).resize(300, 300).into(banner);
        banner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ImageActivity.class);
                intent.putExtra("url", imageUrl);
                intent.putExtra("filename", fileNameList.get(position));
                mContext.startActivity(intent);
            }
        });
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }
}
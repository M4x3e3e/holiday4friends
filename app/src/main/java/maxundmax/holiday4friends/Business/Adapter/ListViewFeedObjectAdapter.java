package maxundmax.holiday4friends.Business.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


import maxundmax.holiday4friends.Business.FirebaseMethods;
import maxundmax.holiday4friends.Business.HolidayObject;
import maxundmax.holiday4friends.Business.MediaObject;
import maxundmax.holiday4friends.Business.ParserHelper;
import maxundmax.holiday4friends.MainActivity;
import maxundmax.holiday4friends.R;

/**
 * Created by Max on 18.04.2018.
 */

public class ListViewFeedObjectAdapter extends BaseAdapter {

    private ArrayList<MediaObject> mediaObjects;
    private ArrayList<HolidayObject> holidayObjects;
    private Context context;

    public ListViewFeedObjectAdapter(ArrayList<MediaObject> _mediaObjects, ArrayList<HolidayObject> _holidayObjects, MainActivity _context) {

        mediaObjects = _mediaObjects;
        holidayObjects = _holidayObjects;
        context = _context;
    }

    @Override
    public int getCount() {
        return mediaObjects.size();
    }

    @Override
    public Object getItem(int i) {
        return mediaObjects.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.custom_media_listview_layout, null);

        ImageView imageView = (ImageView) view.findViewById(R.id.overviewMediaImageViewer);
        TextView textViewName = (TextView) view.findViewById(R.id.overviewMediaTextViewName);
        TextView textViewDescription = (TextView) view.findViewById(R.id.overviewMediaTextViewDescription);

        TextView dayTimeTextView = (TextView) view.findViewById(R.id.textViewTime);
        TextView timeTextView = (TextView) view.findViewById(R.id.overviewMediaTimeTextView);

        textViewName.setText(getHolidayName(mediaObjects.get(i).getHoliday_id()));
        textViewDescription.setText(mediaObjects.get(i).getDescription());
        String feedTime = "";
        if (i == 0) {
            feedTime = ParserHelper.ParsStringToFeed(mediaObjects.get(i).getTimestamp());
            dayTimeTextView.setText(feedTime);
            dayTimeTextView.setVisibility(View.VISIBLE);
        }
        else{
            if(!ParserHelper.ParsStringToFeed(mediaObjects.get(i).getTimestamp()).equals(ParserHelper.ParsStringToFeed(mediaObjects.get(i-1).getTimestamp()))){
                feedTime = ParserHelper.ParsStringToFeed(mediaObjects.get(i).getTimestamp());
                dayTimeTextView.setText(feedTime);
                dayTimeTextView.setVisibility(View.VISIBLE);
            }

        }


        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        timeTextView.setText(sdf.format(mediaObjects.get(i).getTimestamp()));

        if (mediaObjects.get(i).getImage() == null) {
            FirebaseMethods.downloadImageIntoImageView(imageView, mediaObjects.get(i));
        } else {
            imageView.setImageBitmap(mediaObjects.get(i).getImage());
        }
        return view;
    }



    private String getHolidayName(String holidayId) {
        for (HolidayObject h : holidayObjects) {
            if (h.getId().equals(holidayId)) {
                return h.getName();
            }
        }
        return "";

    }
}

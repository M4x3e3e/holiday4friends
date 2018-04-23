package maxundmax.holiday4friends.Business.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import maxundmax.holiday4friends.Business.FirebaseMethods;
import maxundmax.holiday4friends.Business.HolidayObject;
import maxundmax.holiday4friends.Business.MediaObject;
import maxundmax.holiday4friends.MainActivity;
import maxundmax.holiday4friends.R;

/**
 * Created by Max on 18.04.2018.
 */

public class ListViewFeedObjectAdapter extends BaseAdapter  {

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
        view = inflater.inflate(R.layout.custom_media_listview_layout,null);

        ImageView imageView = (ImageView)view.findViewById(R.id.overviewMediaImageViewer);
        TextView textViewName = (TextView)view.findViewById(R.id.overviewMediaTextViewName);
        TextView textViewDescription = (TextView)view.findViewById(R.id.overviewMediaTextViewDescription);

        textViewName.setText(getHolidayName(mediaObjects.get(i).getHoliday_id()));
        textViewDescription.setText(mediaObjects.get(i).getDescription());
        if(mediaObjects.get(i).getImage() == null) {
            FirebaseMethods.downloadImageIntoImageView(imageView, mediaObjects.get(i));
        }else{
            imageView.setImageBitmap(mediaObjects.get(i).getImage());
        } return view;
    }

    private String getHolidayName(String holidayId){
        for (HolidayObject h: holidayObjects) {
            if(h.getId().equals(holidayId)) {
                return h.getName();
            }
        }
        return "";

    }
}

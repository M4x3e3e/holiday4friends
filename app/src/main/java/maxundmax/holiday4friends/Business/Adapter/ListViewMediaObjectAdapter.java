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
import maxundmax.holiday4friends.R;

/**
 * Created by Max on 18.04.2018.
 */

public class ListViewMediaObjectAdapter extends BaseAdapter  {

    private ArrayList<MediaObject> mdList;
    private Context context;
    public ListViewMediaObjectAdapter(ArrayList<MediaObject> _mdList, Context _context)
    {
       mdList = _mdList;
        context = _context;
    }
    @Override
    public int getCount() {
        return mdList.size();
    }

    @Override
    public Object getItem(int i) {
        return mdList.get(i);
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

        textViewName.setText(mdList.get(i).getName());
        textViewDescription.setText(mdList.get(i).getDescription());
        FirebaseMethods.downloadImageIntoImageView(imageView,mdList.get(i).getImagepath());
        return view;
    }
}

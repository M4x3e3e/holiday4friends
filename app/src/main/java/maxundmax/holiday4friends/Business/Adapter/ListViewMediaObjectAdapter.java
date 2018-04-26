package maxundmax.holiday4friends.Business.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import maxundmax.holiday4friends.Business.FirebaseMethods;
import maxundmax.holiday4friends.Business.HolidayObject;
import maxundmax.holiday4friends.Business.MediaObject;
import maxundmax.holiday4friends.R;

/**
 * Created by Max on 18.04.2018.
 */

public class ListViewMediaObjectAdapter extends BaseAdapter
    implements View.OnClickListener{

    private ArrayList<MediaObject> mdList;
    private Context context;
    private boolean isOwnerList;
    public ListViewMediaObjectAdapter(ArrayList<MediaObject> _mdList, Context _context, boolean _isOwnerList)
    {
       mdList = _mdList;
        context = _context;
        isOwnerList = _isOwnerList;


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



        ImageView imageView = view.findViewById(R.id.overviewMediaImageViewer);
        TextView textViewName = view.findViewById(R.id.overviewMediaTextViewName);
        TextView textViewDescription = view.findViewById(R.id.overviewMediaTextViewDescription);
        ImageButton btnDelete = view.findViewById(R.id.overviewMediaBtnDelete);

        textViewName.setVisibility(View.GONE);
        textViewName.setText(mdList.get(i).getName());
        textViewDescription.setText(mdList.get(i).getDescription());
        TextView timeTextView = (TextView) view.findViewById(R.id.overviewMediaTimeTextView);
        if(isOwnerList){
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(this);
            btnDelete.setTag(mdList.get(i).getId());

        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        timeTextView.setText(sdf.format(mdList.get(i).getTimestamp()));
        if(mdList.get(i).getImage() == null) {
            FirebaseMethods.downloadImageIntoImageView(imageView, mdList.get(i));
        }else{
            imageView.setImageBitmap(mdList.get(i).getImage());
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.overviewMediaBtnDelete) {
            ImageButton btn = view.findViewById(R.id.overviewMediaBtnDelete);

            final String someParameter = btn.getTag().toString();

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);
            alertDialogBuilder
                    .setTitle("Are you sure?")
                    .setCancelable(false)
                    .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Do something with parameter.
                            deleteMedia(someParameter);
                        }
                    })
                    .setNegativeButton("Nein",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }

    }


    private void deleteMedia(String mediaObjectId) {
        for (int i = 0; i< mdList.size();i++) {
            if(mdList.get(i).getId().equals(mediaObjectId)){
                mdList.get(i).deleteMedieFromFirebase();
                mdList.remove(i);
                notifyDataSetChanged();
                break;
            }

        }

    }
}

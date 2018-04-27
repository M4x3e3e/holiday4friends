package maxundmax.holiday4friends.Business.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.MutableInt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import maxundmax.holiday4friends.Business.HolidayObject;
import maxundmax.holiday4friends.Business.FirebaseMethods;
import maxundmax.holiday4friends.Business.MediaObject;
import maxundmax.holiday4friends.Business.SubscriptionObject;
import maxundmax.holiday4friends.R;

/**
 * Created by Max on 18.04.2018.
 */

public class ListViewHolidayObjectAdapter extends BaseAdapter
{

    private ArrayList<HolidayObject> acList;

    public Boolean getIsOwnerList() {
        return isOwnerList;
    }

    public void setIsOwnerList(Boolean ownerList) {
        isOwnerList = ownerList;
    }

    private Boolean isOwnerList;
    private Activity context;
    public ListViewHolidayObjectAdapter(ArrayList<HolidayObject> _acList, Activity _context, Boolean _isOwnerList)
    {
        acList = _acList;
        context = _context;
        isOwnerList = _isOwnerList;
    }
    @Override
    public int getCount() {
        return acList.size();
    }

    @Override
    public Object getItem(int i) {
        return acList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.custom_holiday_listview_layout,null);

        ImageView imageView = (ImageView)view.findViewById(R.id.overviewHolidayImageViewer);
        TextView textViewName = (TextView)view.findViewById(R.id.overviewHolidayTextViewName);
        TextView textViewDescription = (TextView)view.findViewById(R.id.overviewHolidayTextViewDescription);
        ImageButton btnSubscription = (ImageButton)view.findViewById(R.id.overviewHolidayBtnSubscription);
        ImageButton btnDelete = (ImageButton)view.findViewById(R.id.overviewHolidayBtnDelete);
        RelativeLayout container = view.findViewById(R.id.holidayContainer);
        TextView subscriptions = view.findViewById(R.id.overviewHolidaySubscriptions);
        ImageView checkMark = view.findViewById(R.id.checkMark);

        if(acList.get(i).getSubscribeCount() > 0){
            subscriptions.setVisibility(View.VISIBLE);
            subscriptions.setText("Bereits "+acList.get(i).getSubscribeCount()+"mal abonniert.");

        }

        if(i % 2 == 1) {
            container.setBackgroundResource(R.color.colorPrimary);
        }
        final int id = i;
        btnSubscription.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SetHolidaySubscription(acList.get(id));
            }
        });

        final HolidayObject hObj = acList.get(i);



        if(!isOwnerList && acList.get(i).getSubscriptionObject() == null){

            btnSubscription.setImageResource(R.drawable.ic_notifications_active_black_24dp);
            btnSubscription.setVisibility(View.VISIBLE);

        }
        if(!isOwnerList && acList.get(i).getSubscriptionObject() != null) {
            btnSubscription.setImageResource(R.drawable.ic_notifications_off_black_24dp);
            btnSubscription.setVisibility(View.VISIBLE);
            checkMark.setVisibility(View.VISIBLE);

        }

        textViewName.setText(acList.get(i).getName());
        textViewDescription.setText(acList.get(i).getDescription());
        FirebaseMethods.downloadImageIntoImageView(imageView,acList.get(i));
        return view;
    }

    private void SetHolidaySubscription(HolidayObject holidayObject){

        if(holidayObject.getSubscriptionObject() == null) {
            SubscriptionObject subscriptionObject = new SubscriptionObject();
            subscriptionObject.setHoliday_id(holidayObject.getId());
            subscriptionObject.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
            subscriptionObject.uploadSubScriptionToFirebase(context);
            holidayObject.setSubscriptionObject(subscriptionObject);
            holidayObject.setSubscribeCount(holidayObject.getSubscribeCount()+1);
            this.notifyDataSetChanged();
        } else {
            SubscriptionObject obj = holidayObject.getSubscriptionObject();

            holidayObject.setSubscribeCount(holidayObject.getSubscribeCount()-1);
            holidayObject.setSubscriptionObject(null);
            obj.delete();
            this.notifyDataSetChanged();
        }
    }


}

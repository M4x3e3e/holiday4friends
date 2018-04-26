package maxundmax.holiday4friends.Business.Adapter;

import android.app.Activity;
import android.content.Context;
import android.util.MutableInt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import maxundmax.holiday4friends.Business.HolidayObject;
import maxundmax.holiday4friends.Business.FirebaseMethods;
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
        ImageView imageViewSubscriped = view.findViewById(R.id.overviewHolidayImageViewSubscription);

        if(!isOwnerList && acList.get(i).getSubscriptionObject() == null){
            btnSubscription.setVisibility(View.VISIBLE);
            final int id = i;
            btnSubscription.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    SetHolidaySubscription(acList.get(id));
                }
            });
        }
        if(!isOwnerList && acList.get(i).getSubscriptionObject() != null) {
            imageViewSubscriped.setVisibility(View.VISIBLE);
        }

        textViewName.setText(acList.get(i).getName() + "("+acList.get(i).getSubscribeCount().value+")");
        textViewDescription.setText(acList.get(i).getDescription());
        FirebaseMethods.downloadImageIntoImageView(imageView,acList.get(i));
        return view;
    }

    private void SetHolidaySubscription(HolidayObject holidayObject){
        SubscriptionObject subscriptionObject = new SubscriptionObject();
        subscriptionObject.setHoliday_id(holidayObject.getId());
        subscriptionObject.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        subscriptionObject.uploadSubScriptionToFirebase(context);
        holidayObject.setSubscriptionObject(subscriptionObject);
        this.notifyDataSetChanged();
    }


}

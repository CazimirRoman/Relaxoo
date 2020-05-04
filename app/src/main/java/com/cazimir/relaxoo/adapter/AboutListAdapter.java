package com.cazimir.relaxoo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.model.AboutItem;
import com.cazimir.relaxoo.model.MenuItemType;

import java.util.List;

public class AboutListAdapter extends RecyclerView.Adapter<AboutListAdapter.RowHolder> {

    private static final String TAG = AboutListAdapter.class.getSimpleName();
    private Context context;
    private List<AboutItem> data;
    private Interactor interactor;

    public AboutListAdapter(Context context, List<AboutItem> data, Interactor interactor) {
        this.context = context;
        this.data = data;
        this.interactor = interactor;
    }

    /**
     * Inflate custom layout to use
     *
     * @param parent
     * @param viewType
     * @return new instance of ViewHolder with the inflated view.
     */
    @Override
    public RowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_about, parent, false);
        return new RowHolder(view);
    }

    /**
     * get item from list set listeners for views
     *
     * @param rowholder
     * @param position
     */
    @Override
    public void onBindViewHolder(RowHolder rowholder, final int position) {

        final AboutItem item = data.get(position);

        // example
        rowholder.name.setText(item.getName().getItemName());
        rowholder.icon.setImageResource(item.getIcon());

        rowholder.rootLayout.setOnClickListener(view -> interactor.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void removeRemoveAds() {

        AboutItem itemToBeDeleted = null;

        for (AboutItem aboutItem : data) {
            if (aboutItem.getName() == MenuItemType.REMOVE_ADS) {
                itemToBeDeleted = aboutItem;
                break;
            }
        }

        int index = data.indexOf(itemToBeDeleted);
        data.remove(index);
        notifyItemRemoved(index);
    }

    public interface Interactor {
        void onItemClick(AboutItem item);
    }

    /**
     * RowHolder holds reference to all views int he layout
     */
    public static class RowHolder extends RecyclerView.ViewHolder {

        private LinearLayout rootLayout;
        private AppCompatTextView name;
        private ImageView icon;

        public RowHolder(View view) {
            super(view);
            // example
            name = view.findViewById(R.id.about_item_name);
            icon = view.findViewById(R.id.about_item_logo);
            rootLayout = view.findViewById(R.id.rootLayout);

            // rest of the views
        }
    }
}

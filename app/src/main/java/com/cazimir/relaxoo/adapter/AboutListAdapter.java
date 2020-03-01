package com.cazimir.relaxoo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.model.AboutItem;

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
        rowholder.name.setText(item.getName().toString());
        rowholder.icon.setImageResource(item.getIcon());

        rowholder.name.setOnClickListener(view -> interactor.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface Interactor {
        void onItemClick(AboutItem item);
    }

    /**
     * RowHolder holds reference to all views int he layout
     */
    public static class RowHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private ImageView icon;

        public RowHolder(View view) {
            super(view);
            // example
            name = view.findViewById(R.id.about_item_name);
            icon = view.findViewById(R.id.about_item_logo);

            // rest of the views
        }
    }
}

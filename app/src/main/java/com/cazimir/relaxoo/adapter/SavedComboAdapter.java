package com.cazimir.relaxoo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.model.SavedCombo;

import java.util.List;

public class SavedComboAdapter extends
        RecyclerView.Adapter<SavedComboAdapter.ViewHolder> {

    private static final String TAG = SavedComboAdapter.class.getSimpleName();

    private Context context;
    private List<SavedCombo> list;

    public SavedComboAdapter(Context context, List<SavedCombo> list) {
        this.context = context;
        this.list = list;
    }

    public void addCombo(SavedCombo savedCombo) {
        list.add(savedCombo);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;

        public ViewHolder(View view) {
            super(view);

            textView = (TextView) itemView.findViewById(R.id.comboText);
            // rest of the views

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_saved_combo, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // get element from your dataset at this position
        SavedCombo item = list.get(position);
        // replace the contents of the view with that element
        holder.textView.setText(item.name());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
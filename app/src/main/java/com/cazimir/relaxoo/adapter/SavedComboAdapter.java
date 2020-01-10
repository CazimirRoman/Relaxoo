package com.cazimir.relaxoo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.model.SavedCombo;

import java.util.List;

public class SavedComboAdapter extends RecyclerView.Adapter<SavedComboAdapter.RowHolder> {

  private static final String TAG = SavedComboAdapter.class.getSimpleName();
  private final OnItemClickListener listener;
  private Context context;
  private List<SavedCombo> list;

  public SavedComboAdapter(Context context, List<SavedCombo> list, OnItemClickListener listener) {
    this.context = context;
    this.list = list;
    this.listener = listener;
  }

  public void addCombo(SavedCombo savedCombo) {
   list.add(savedCombo);
  }

  /** Inflate custom layout to use
   * @param parent
   * @param viewType
   * @return new instance of ViewHolder with the inflated view.
   */
  @NonNull
  @Override
  public RowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);
    View view = inflater.inflate(R.layout.item_saved_combo, parent, false);
    return new RowHolder(view);
  }

  /** get item from list
   * set listeners for views
   * @param holder
   * @param position
   */
  @Override
  public void onBindViewHolder(RowHolder holder, final int position) {

    final SavedCombo savedCombo = list.get(position);

    holder.comboText.setText(savedCombo.name());
    holder.parentLayout.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Log.d(TAG, "Clicked on combo in favorites list with active sounds: " + savedCombo.getSoundPoolParameters().toString());
            listener.onItemClick(list.get(position));
          }
        });

    holder.deleteCombo.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        listener.onItemDeleted(position);
      }
    });
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  public void removeCombo(int position) {
    list.remove(list.get(position));
    notifyDataSetChanged();
  }

  public interface OnItemClickListener {
    void onItemClick(SavedCombo savedCombo);
    void onItemDeleted(int position);
  }

  /**
   * is responsible for binding data as needed from our model into the widgets for a row
   */
  static class RowHolder extends RecyclerView.ViewHolder {

    private TextView comboText;
    private ImageButton deleteCombo;
    private ConstraintLayout parentLayout;

    RowHolder(View view) {
      super(view);
      comboText = view.findViewById(R.id.comboText);
      parentLayout = view.findViewById(R.id.parentLayout);
      deleteCombo = view.findViewById(R.id.deleteCombo);

      // rest of the views


    }
  }
}

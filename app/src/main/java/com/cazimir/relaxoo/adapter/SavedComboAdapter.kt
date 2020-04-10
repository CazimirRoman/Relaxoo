package com.cazimir.relaxoo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.model.ListOfSavedCombos
import com.cazimir.relaxoo.model.SavedCombo

class SavedComboAdapter(private val context: Context, private val list: ListOfSavedCombos, private val listener: OnItemClickListener) : RecyclerView.Adapter<SavedComboAdapter.ViewHolder>() {

    /** Inflate custom layout to use
     * @param parent
     * @param viewType
     * @return new instance of ViewHolder with the inflated view.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_saved_combo, parent, false)
        return ViewHolder(view)
    }

    /** get item from list
     * set listeners for views
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val savedCombo = list.savedComboList!![position]
        holder.comboText.text = savedCombo.name()
        holder.parentLayout.setOnClickListener { //Log.d(TAG, "Clicked on combo in favorites list with active sounds: " + savedCombo.getSounds().toString());
            listener.onItemClick(list.savedComboList!![position])
        }
        holder.deleteCombo.setOnClickListener { listener.onItemDeleted(position) }
    }

    override fun getItemCount(): Int {
        return list.savedComboList!!.size
    }

    interface OnItemClickListener {
        fun onItemClick(savedCombo: SavedCombo)
        fun onItemDeleted(position: Int)
    }

    /**
     * is responsible for binding data as needed from our model into the widgets for a row
     */
    class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val comboText: TextView
        val deleteCombo: ImageButton
        val parentLayout: ConstraintLayout

        init {
            comboText = view.findViewById(R.id.comboText)
            parentLayout = view.findViewById(R.id.parentLayout)
            deleteCombo = view.findViewById(R.id.deleteCombo)

            // rest of the views
        }
    }

    companion object {
        private val TAG = SavedComboAdapter::class.java.simpleName
    }

}
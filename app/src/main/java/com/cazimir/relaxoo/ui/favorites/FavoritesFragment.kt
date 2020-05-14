package com.cazimir.relaxoo.ui.favorites

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cazimir.relaxoo.MainActivity
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.adapter.SavedComboAdapter
import com.cazimir.relaxoo.analytics.AnalyticsEvents.Companion.comboDeleted
import com.cazimir.relaxoo.analytics.AnalyticsEvents.Companion.comboTriggered
import com.cazimir.relaxoo.dialog.OnDeleted
import com.cazimir.relaxoo.dialog.favorite.FavoriteDeleted
import com.cazimir.relaxoo.model.ListOfSavedCombos
import com.cazimir.relaxoo.model.SavedCombo
import com.cazimir.relaxoo.ui.sound_grid.OnActivityCallback
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.favorites_fragment.view.*

class FavoritesFragment : Fragment() {
    private lateinit var favoritesFragmentView: View
    private lateinit var viewModel: FavoritesViewModel
    private lateinit var adapter: SavedComboAdapter
    private var activityCallback: OnActivityCallback? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        favoritesFragmentView = inflater.inflate(R.layout.favorites_fragment, container, false)
        return favoritesFragmentView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FavoritesViewModel::class.java)
        viewModel.repository = FavoritesRepository()
        viewModel.fetchFavorites()
        viewModel
                .savedCombosLive()
                .observe(
                        viewLifecycleOwner,
                        Observer { savedCombos: ListOfSavedCombos ->
                            // update recyclerview
                            favoritesFragmentView.favoritesList.layoutManager = LinearLayoutManager(context)
                            adapter = SavedComboAdapter(context!!, savedCombos, object : SavedComboAdapter.OnItemClickListener {
                                private var positionToBeDeleted = 0
                                override fun onItemClick(savedCombo: SavedCombo) {
                                    FirebaseAnalytics.getInstance(context!!).logEvent(comboTriggered().first, comboTriggered().second)
                                    activityCallback?.triggerCombo(savedCombo)
                                }

                                override fun onItemDeleted(position: Int) {
                                    FirebaseAnalytics.getInstance(context!!).logEvent(comboDeleted().first, comboDeleted().second)
                                    positionToBeDeleted = position
                                    val deleted: OnDeleted = object : FavoriteDeleted {
                                        override fun deleted() {
                                            deleteFavorite(positionToBeDeleted)
                                        }
                                    }
                                    activityCallback?.showDeleteConfirmationDialog(deleted)
                                }
                            })
                            favoritesFragmentView.favoritesList.adapter = adapter
                            favoritesFragmentView.no_favorites_text.visibility = if (savedCombos.savedComboList!!.size != 0) View.GONE else View.VISIBLE
                        })
    }

    fun updateList(savedCombo: SavedCombo?) {
        viewModel.addFavorite(savedCombo!!)
    }

    fun deleteFavorite(position: Int) {
        viewModel.deleteCombo(position)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) activityCallback = context as OnActivityCallback
    }

    companion object {
        private const val TAG = "FavoritesFragment"
        fun newInstance(): FavoritesFragment {
            return FavoritesFragment()
        }
    }
}

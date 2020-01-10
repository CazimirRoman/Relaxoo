package com.cazimir.relaxoo.ui.favorites;

import com.cazimir.relaxoo.model.SavedCombo;

/** using interfaces to create fake implementations for tests. see Elegant Objects */
interface IFavoritesFragment {
  void updateList(SavedCombo soundName);

  void deleteFavorite(int position);
}

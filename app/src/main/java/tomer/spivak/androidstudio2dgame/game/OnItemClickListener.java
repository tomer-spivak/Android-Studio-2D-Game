package tomer.spivak.androidstudio2dgame.game;

import tomer.spivak.androidstudio2dgame.buildingHelper.Building;

public interface OnItemClickListener {
    void onBuildingRecyclerViewItemClick(Building building, int position);
}
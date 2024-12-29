package tomer.spivak.androidstudio2dgame.buildingHelper;

import android.graphics.Point;
import android.widget.ImageView;

public class BuildingView extends Building {
    ImageView view;
    Point point;


    //public BuildingView(String imageUrl, String title, ImageView view) {
      //  super(imageUrl, title);
        //this.view = view;
    //}
    public BuildingView(Building building, ImageView view){
        super(building.getImageUrl(), building.getName());
        this.view = view;
    }

    public ImageView getView() {
        return view;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }
}

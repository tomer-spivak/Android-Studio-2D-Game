package tomer.spivak.androidstudio2dgame.gameActivity;

public class BuildingToPick {
    private int imageUrl;
    private String name;

    public BuildingToPick(String name, int imageUrl) {
        this.imageUrl = imageUrl;
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public int getImageUrl() {
        return imageUrl;
    }
}

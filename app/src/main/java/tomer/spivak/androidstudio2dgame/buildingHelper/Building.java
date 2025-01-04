package tomer.spivak.androidstudio2dgame.buildingHelper;

public class Building {
    private int imageUrl;
    private String name; // Optional: if you want a caption

    public Building(int imageUrl, String title) {
        this.imageUrl = imageUrl;
        this.name = title;
    }

    // Getters
    public int getImageUrl() { return imageUrl; }
    public String getName() { return name; }
}

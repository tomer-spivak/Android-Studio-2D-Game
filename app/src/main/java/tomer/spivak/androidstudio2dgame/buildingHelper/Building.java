package tomer.spivak.androidstudio2dgame.buildingHelper;

public class Building {
    private String imageUrl;
    private String name; // Optional: if you want a caption

    public Building(String imageUrl, String title) {
        this.imageUrl = imageUrl;
        this.name = title;
    }

    // Getters
    public String getImageUrl() { return imageUrl; }
    public String getName() { return name; }
}

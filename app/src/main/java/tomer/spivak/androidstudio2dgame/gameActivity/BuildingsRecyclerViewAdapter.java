package tomer.spivak.androidstudio2dgame.gameActivity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;


import tomer.spivak.androidstudio2dgame.R;

public class BuildingsRecyclerViewAdapter extends
        RecyclerView.Adapter<BuildingsRecyclerViewAdapter.BuildingViewHolder> {


    private final Context context;
    private final String[] buildingImages;
    private View selectedBuilding;
    private final GameActivity gameActivity; // Add reference to Activity

    // Updated constructor
    public BuildingsRecyclerViewAdapter(Context context,
                                        String[] buildingImages,
                                        GameActivity activity) {
        this.context = context;
        this.buildingImages = buildingImages;
        this.gameActivity = activity; // Store activity reference
    }

    @NonNull
    @Override
    public BuildingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_building_item, parent, false);
        return new BuildingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuildingViewHolder holder, int position) {
        String imageName = buildingImages[(position)].toLowerCase();
        int resourceId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        Log.d("debug", imageName);
        Glide.with(context)
                .load(resourceId)
                .placeholder(R.drawable.placeholder_building)
                .into(holder.imageView);

        String title = imageName.replace("0", " ");
        String[] words = title.split(" ");
        for (int i = 0; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
            }
        }
        title = String.join(" ", words);
        holder.tvName.setText(title);

        if (title.equals("Lightning Tower")){
            holder.tvPrice.setText("3,000 \uD83D\uDCB0");
        }
        if (title.equals("Obelisk")){
            holder.tvPrice.setText("1,000 \uD83D\uDCB0");
        }

    }

    @Override
    public int getItemCount() {
        return buildingImages.length;
    }

    public class BuildingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView tvName;
        TextView tvPrice;

        public BuildingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            tvName = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (selectedBuilding != null) {
                selectedBuilding.setSelected(false);
            }
            selectedBuilding = itemView;
            itemView.setSelected(true);

            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                String buildingImageURL = buildingImages[position];
                // Directly call Activity methods
                gameActivity.onBuildingSelected(buildingImageURL.replace("0", ""));
                gameActivity.closeBuildingMenu(); // New helper method
            }
        }
    }
}

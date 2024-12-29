package tomer.spivak.androidstudio2dgame.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.buildingHelper.Building;

public class BuildingsRecyclerViewAdapter extends RecyclerView.Adapter<BuildingsRecyclerViewAdapter.BuildingViewHolder>{

    private final Context context;

    private final ArrayList<Building> buildingArrayList;

    private View selectedBuilding;

    BuildingsRecyclerViewAdapter(Context context, ArrayList<Building> buildingArrayList){
        this.context = context;
        this.buildingArrayList = buildingArrayList;
    }

    @NonNull
    @Override
    public BuildingsRecyclerViewAdapter.BuildingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_building_item, parent, false);
        return new BuildingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuildingsRecyclerViewAdapter.BuildingViewHolder holder, int position) {
        Building building = buildingArrayList.get(position);

        // Load image with Glide
        Glide.with(context)
                .load(building.getImageUrl())
                .placeholder(R.drawable.placeholder_building)
//                .error(R.drawable.error_image)
                .into(holder.imageView);

        // Optional: Set title if you want captions
        holder.tvName.setText(building.getName());
    }

    @Override
    public int getItemCount() {
        return buildingArrayList.size();
    }

    public Building getSelectedBuilding() {
        ImageView imageView = selectedBuilding.findViewById(R.id.imageView);
        TextView tvName = selectedBuilding.findViewById(R.id.tvName);
        String imageUrl = (String) imageView.getTag();
        String title = tvName.getText().toString();
        return new Building(imageUrl, title);
    }
    public View getSelectedBuildingView() {
        return selectedBuilding;
    }


    public class BuildingViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView tvName;

        public BuildingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            tvName = itemView.findViewById(R.id.tvName);

            itemView.setOnClickListener(v -> {
                if(selectedBuilding != null){
                    selectedBuilding.setSelected(false);
                    selectedBuilding.setBackgroundColor(0);
                }

                selectedBuilding = itemView;
                itemView.setSelected(true);
                itemView.setBackgroundColor(-16776961);
                Toast.makeText(context, "hello", Toast.LENGTH_SHORT).show();
            });
        }
    }
}

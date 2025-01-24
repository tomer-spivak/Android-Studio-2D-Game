package tomer.spivak.androidstudio2dgame.gameActivity;

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

public class BuildingsRecyclerViewAdapter extends RecyclerView.Adapter<BuildingsRecyclerViewAdapter.BuildingViewHolder>{

    private final Context context;

    private final ArrayList<BuildingToPick> buildingArrayList;

    private final OnItemClickListener listener;

    private View selectedBuilding;

    BuildingsRecyclerViewAdapter(Context context, ArrayList<BuildingToPick> buildingArrayList, OnItemClickListener listener){
        this.context = context;
        this.buildingArrayList = buildingArrayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BuildingsRecyclerViewAdapter.BuildingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_building_item, parent, false);

        return new BuildingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuildingsRecyclerViewAdapter.BuildingViewHolder holder, int position) {
        BuildingToPick building = buildingArrayList.get(position);

        // Load image with Glide
        Glide.with(context)
                .load(building.getImageUrl())
                .placeholder(R.drawable.placeholder_building)
//                .error(R.drawable.error_image)
                .into(holder.imageView);

        // Optional: Set title if you want captions
        holder.tvName.setText(building.getName());

        holder.bind(buildingArrayList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return buildingArrayList.size();
    }




    public class BuildingViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView tvName;

        public BuildingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            tvName = itemView.findViewById(R.id.tvName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedBuilding != null) {
                        selectedBuilding.setSelected(false);
                    }

                selectedBuilding = itemView;
                itemView.setSelected(true);
                Toast.makeText(context, "hello", Toast.LENGTH_SHORT).show();
                }
            }) ;
        }
        public void bind(BuildingToPick building, int position) {
            //tvName.setText(item);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBuildingRecyclerViewItemClick(building, position);
                }
            });
        }
    }
}

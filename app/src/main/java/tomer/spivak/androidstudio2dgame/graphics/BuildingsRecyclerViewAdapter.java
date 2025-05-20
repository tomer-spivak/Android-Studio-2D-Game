package tomer.spivak.androidstudio2dgame.graphics;

import android.content.Context;

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
    private final int[] buildingImagesRes;
    private View selectedBuilding;
    private final OnBuildingClickListener listener;

    public BuildingsRecyclerViewAdapter(Context context, int[] buildingImagesRes, OnBuildingClickListener listener) {
        this.context = context;
        this.buildingImagesRes = buildingImagesRes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BuildingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_building_item, parent, false);
        return new BuildingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuildingViewHolder holder, int position) {
        int resourceId = buildingImagesRes[position];
        Glide.with(context).load(resourceId).placeholder(R.drawable.placeholder_building).into(holder.imageView);
        String imageName = context.getResources().getResourceEntryName(resourceId);
        String[] parts = imageName.split("_");

        String title = "";
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                parts[i] = parts[i].substring(0,1).toUpperCase() + parts[i].substring(1).toLowerCase();
                title += parts[i] + " ";
            }
        }
        title = title.substring(0, title.length()-1);

        holder.tvName.setText(title);
        if (title.equals("Lightning Tower")){
            holder.tvPrice.setText("3,000 \uD83D\uDCB0");
        }
        if (title.equals("Obelisk")){
            holder.tvPrice.setText("1,000 \uD83D\uDCB0");
        }
        if(title.equals("Exploding Tower")){
            holder.tvPrice.setText("2,000 \uD83D\uDCB0");
        }
    }

    @Override
    public int getItemCount() {
        return buildingImagesRes.length;
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

            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                String buildingImageURL = context.getResources().getResourceEntryName(buildingImagesRes[position]).replace("_","");
                listener.onBuildingSelected(buildingImageURL);
                listener.onCloseBuildingMenu();
            }
        }
    }
    public interface OnBuildingClickListener {
        void onBuildingSelected(String buildingType);
        void onCloseBuildingMenu();
    }
}
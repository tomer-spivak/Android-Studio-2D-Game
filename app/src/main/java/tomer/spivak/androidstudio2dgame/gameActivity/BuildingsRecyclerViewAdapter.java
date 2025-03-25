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

import java.util.ArrayList;

import tomer.spivak.androidstudio2dgame.R;

public class BuildingsRecyclerViewAdapter extends
        RecyclerView.Adapter<BuildingsRecyclerViewAdapter.BuildingViewHolder> {

    private final Context context;
    private final ArrayList<String> buildingArrayList;
    private final OnItemClickListener listener;
    private View selectedBuilding;

    public BuildingsRecyclerViewAdapter(Context context, ArrayList<String> buildingArrayList,
                                            OnItemClickListener listener) {
        this.context = context;
        this.buildingArrayList = buildingArrayList;
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
        String imageName = buildingArrayList.get(position).toLowerCase();
        int resourceId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        Log.d("debug", imageName);
        Glide.with(context)
                .load(resourceId)
                .placeholder(R.drawable.placeholder_building)
                .into(holder.imageView);

        // Format title: replace "0" with space, then capitalize each word.
        String title = imageName.replace("0", " ");
        String[] words = title.split(" ");
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() > 0) {
                words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
            }
        }
        title = String.join(" ", words);
        holder.tvName.setText(title);
    }

    @Override
    public int getItemCount() {
        return buildingArrayList.size();
    }

    public class BuildingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView tvName;

        public BuildingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            tvName = itemView.findViewById(R.id.tvName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Manage selected state
            if (selectedBuilding != null) {
                selectedBuilding.setSelected(false);
            }
            selectedBuilding = itemView;
            itemView.setSelected(true);

            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                String buildingImageURL = buildingArrayList.get(position);
                listener.onBuildingRecyclerViewItemClick(buildingImageURL, position);
            }
        }
    }
}

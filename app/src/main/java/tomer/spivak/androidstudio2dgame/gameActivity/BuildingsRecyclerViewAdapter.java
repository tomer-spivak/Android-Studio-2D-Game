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
        RecyclerView.Adapter<BuildingsRecyclerViewAdapter.BuildingViewHolder>{

    private final Context context;

    private final ArrayList<String> buildingArrayList;

    private final OnItemClickListener listener;

    private View selectedBuilding;

    BuildingsRecyclerViewAdapter(Context context, ArrayList<String> buildingArrayList, OnItemClickListener listener){
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
        String imageUrl = buildingArrayList.get(position).toLowerCase();
        int resourceId = context.getResources().getIdentifier(imageUrl,
                "drawable", context.getPackageName());
        Log.d("debug", imageUrl);
        Glide.with(context)
                .load(resourceId)
                .placeholder(R.drawable.placeholder_building)
                .into(holder.imageView);

        String title = imageUrl.replace("0", " ");
        String[] words = title.split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            word = word.substring(0, 1).toUpperCase() + word.substring(1);
            words[i] = word;
        }
        title = String.join(" ", words);
        holder.tvName.setText(title);

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
                }
            }) ;
        }
        public void bind(String buildingImageURL, int position) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onBuildingRecyclerViewItemClick(buildingImageURL, position);
                    }
                }
            });
        }
    }
}

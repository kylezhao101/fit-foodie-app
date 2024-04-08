package com.example.fitnessgachaapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GachaAdapter extends RecyclerView.Adapter<GachaAdapter.ViewHolder>{
    private List<GachaItem> gachaList;
    private Context context;

    public GachaAdapter(List<GachaItem> gachaList, Context context) {
        this.gachaList = gachaList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gacha_item_layout, parent, false);
        return new ViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GachaItem item = gachaList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return gachaList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView spriteView;
        TextView nameView;
        private Context context;

        ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            spriteView = itemView.findViewById(R.id.gachaSpriteImageView);
            nameView = itemView.findViewById(R.id.gachaNameTextView);
        }

        void bind(GachaItem item) {
            String displayName = item.getDupeCount() > 0 ? (item.getDupeCount() + 1) + " " + item.getName() : item.getName();
            nameView.setText(displayName);

            // Use context from the adapter to get resources
            int drawableResourceId = this.context.getResources().getIdentifier(item.getName().toLowerCase(), "drawable", this.context.getPackageName());
            if (drawableResourceId != 0) { // Resource found
                Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), drawableResourceId);
                Bitmap scaledBitmap = scalePixelArt(originalBitmap, 18);
                spriteView.setImageBitmap(scaledBitmap);
            } else {
                // Handle the case where the drawable resource is not found
                spriteView.setImageResource(R.drawable.beer);
            }
        }

        public static Bitmap scalePixelArt(Bitmap bitmap, int scale) {
            Bitmap scaledBitmap = Bitmap.createBitmap(bitmap.getWidth() * scale, bitmap.getHeight() * scale, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(scaledBitmap);
            Paint paint = new Paint();
            paint.setFilterBitmap(false); // Disables bilinear filtering
            canvas.scale(scale, scale);
            canvas.drawBitmap(bitmap, 0, 0, paint);
            return scaledBitmap;
        }
    }
}

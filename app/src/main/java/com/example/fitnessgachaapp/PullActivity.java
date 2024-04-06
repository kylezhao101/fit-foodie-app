package com.example.fitnessgachaapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class PullActivity extends AppCompatActivity implements SensorEventListener {


    private static final int[] drawableIds = {
            R.drawable.avocado,
            R.drawable.bacon,
            R.drawable.beer,
            R.drawable.boar,
            R.drawable.bread,
            R.drawable.brownie,
            R.drawable.bug,
            R.drawable.cheese,
            R.drawable.cherry,
            R.drawable.chicken,
            R.drawable.chickenleg,
            R.drawable.cookie,
            R.drawable.dragonfruit,
            R.drawable.eggplant,
            R.drawable.eggs,
            R.drawable.fish,
            R.drawable.fishfillet,
            R.drawable.fishsteak,
            R.drawable.grub,
            R.drawable.grubs,
            R.drawable.honey,
            R.drawable.honeycomb,
            R.drawable.jam,
            R.drawable.jerky,
            R.drawable.lemon,
            R.drawable.marmalade,
            R.drawable.meloncantaloupe,
            R.drawable.melonhoneydew,
            R.drawable.melonwater,
            R.drawable.moonshine,
            R.drawable.olive,
            R.drawable.onion,
            R.drawable.peach,
            R.drawable.peppergreen,
            R.drawable.pepperoni,
            R.drawable.pepperred,
            R.drawable.pickle,
            R.drawable.pickledeggs,
            R.drawable.pieapple,
            R.drawable.pielemon,
            R.drawable.piepumpkin,
            R.drawable.pineapple,
            R.drawable.potato,
            R.drawable.potatored,
            R.drawable.pretzel,
            R.drawable.ribs,
            R.drawable.roll,
            R.drawable.saki,
            R.drawable.sardines,
            R.drawable.sashimi,
            R.drawable.sausages,
            R.drawable.shrimp,
            R.drawable.steak,
            R.drawable.stein,
            R.drawable.strawberry,
            R.drawable.sushi,
            R.drawable.tart,
            R.drawable.tomato,
            R.drawable.turnip,
            R.drawable.waffles,
            R.drawable.whiskey,
            R.drawable.wine
    };

    private Button returnButton;
    private TextView foodText;
    private ImageView randomImageView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pull_page);

        //layout setup
        returnButton = findViewById(R.id.returnButton);
        foodText = findViewById(R.id.foodName);
        randomImageView = findViewById(R.id.FoodImageView);

        //random food item in pulled
        int randomDrawableId = getRandomDrawableId();
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), randomDrawableId);
        Bitmap scaledBitmap = scalePixelArt(originalBitmap, 5);
        randomImageView.setImageBitmap(scaledBitmap);
        foodText.setText(randomDrawableId);

        returnButton.setOnClickListener(view -> {
            Intent intent = new Intent(PullActivity.this, GachaActivity.class);
            startActivity(intent);
        });
    }

    private int getRandomDrawableId() {
        Random random = new Random();
        int index = random.nextInt(drawableIds.length);
        return drawableIds[index];
    }

    public static Bitmap scalePixelArt(Bitmap bitmap, int scale) {
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap.getWidth() * scale, bitmap.getHeight() * scale, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaledBitmap);
        Paint paint = new Paint();
        paint.setFilterBitmap(false);
        canvas.scale(scale, scale);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return scaledBitmap;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

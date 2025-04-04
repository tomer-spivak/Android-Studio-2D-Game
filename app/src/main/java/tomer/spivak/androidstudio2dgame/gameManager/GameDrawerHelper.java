package tomer.spivak.androidstudio2dgame.gameManager;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import java.text.NumberFormat;

import tomer.spivak.androidstudio2dgame.gameObjects.GameBuilding;
import tomer.spivak.androidstudio2dgame.gameObjects.GameObject;
import tomer.spivak.androidstudio2dgame.gameObjects.GameObjectManager;

public class GameDrawerHelper {

    private final GameObjectManager gameObjectManager;
    Paint timerPaint = new Paint();
    Rect timerBounds = new Rect();
    private final Paint backgroundPaint = new Paint();
    public GameDrawerHelper(GameObjectManager gameObjectManager){
        this.gameObjectManager = gameObjectManager;


        timerPaint.setColor(Color.WHITE);
        timerPaint.setTextSize(60);
        timerPaint.setAntiAlias(true);
        timerPaint.setTextAlign(Paint.Align.LEFT);
        timerPaint.setShadowLayer(5, 0, 0, Color.BLACK);


    }

    public void drawHealthBar(GameObject gameObject, Canvas canvas, float scale) {
        Point pos = gameObject.getImagePoint();
        tomer.spivak.androidstudio2dgame.model.Position position = gameObject.getPos();
        float health = gameObjectManager.getBoard()[position.getX()][position.getY()].getObject().getHealth();
        float maxHealth = gameObjectManager.getBoard()[position.getX()][position.getY()].getObject().getMaxHealth();
        int barWidth = (int) (80 * scale);
        int barHeight = (int) (15 * scale);
        int x = pos.x - barWidth / 2;
        int yOffset;
        if (gameObject instanceof GameBuilding){
            yOffset = 290;
        } else {
            yOffset = 170;
        }
        int y = (int) (pos.y - yOffset * scale);
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.GRAY);
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(x, y, x + barWidth, y + barHeight, bgPaint);
        Paint healthPaint = new Paint();
        healthPaint.setColor(Color.RED);
        healthPaint.setStyle(Paint.Style.FILL);
        int healthWidth = (int) ((health / maxHealth) * barWidth);
        canvas.drawRect(x, y, x + healthWidth, y + barHeight, healthPaint);
    }

    public void drawTimeTillNextRound(Canvas canvas, long timeTillNextRound, int screenWidth) {
        String timeText = "Next round: " + (timeTillNextRound / 1000) + "." +
                (timeTillNextRound % 1000) / 100 + "s";
        timerPaint.getTextBounds(timeText, 0, timeText.length(), timerBounds);
        int x = screenWidth / 2 - timerBounds.width() / 2;
        int y = 100;
        canvas.drawText(timeText, x, y, timerPaint);
    }

    public void drawRoundNumber(Canvas canvas, int currentRound, int screenWidth){
        String roundText = "current round: " + currentRound;
        timerPaint.getTextBounds(roundText, 0, roundText.length(), timerBounds);
        int x = screenWidth / 2 - timerBounds.width() / 2;
        int y = 100;
        canvas.drawText(roundText, x, y, timerPaint);
    }

    public void drawBackground(Canvas canvas, Bitmap backgroundBitmap, int screenWidth, int screenHeight) {
        Bitmap scaledBackBitmap = Bitmap.createScaledBitmap(backgroundBitmap, screenWidth, screenHeight, true);
        canvas.drawBitmap(scaledBackBitmap, 0, 0, backgroundPaint);
    }


    public void drawShnuzes(Canvas canvas, int shunzes, int screenWidth) {
        String formattedShunzes = NumberFormat.getNumberInstance().format(shunzes);

        String shunzesText = "Shnuzes: " + formattedShunzes + " \uD83D\uDCB0";

        int x = screenWidth / 2 - timerBounds.width() / 2 - 50;
        int y = 180;

        canvas.drawText(shunzesText, x, y, timerPaint);
    }



}

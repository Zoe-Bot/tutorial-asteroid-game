package tutorial;


import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import javafx.animation.Interpolator;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;


import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;

public class App extends GameApplication {

    private Entity player;

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(1280);
        gameSettings.setHeight(720);
        gameSettings.setTitle("Asteroids");
        gameSettings.setVersion("0.1");
    }

    @Override
    protected void initInput() {
        onKey(KeyCode.A, () -> player.getComponent(PlayerComponent.class).rotateLeft());
        onKey(KeyCode.D, () -> player.getComponent(PlayerComponent.class).rotateRight());
        onKey(KeyCode.W, () -> player.getComponent(PlayerComponent.class).move());

        onKeyDown(KeyCode.SPACE, "Shoot", () -> player.getComponent(PlayerComponent.class).shoot());
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
        vars.put("lives", 3);
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new GameEntityFactory());

        //sound
        getSettings().setGlobalSoundVolume(0.1);
        //loopBGM("hiereineMp3Datei");

        spawn("background");

        player = spawn("player", getAppWidth() / 2, getAppHeight() / 2);

        run(() -> {

            Entity e = getGameWorld().create("asteroid", new SpawnData(100, 100));

            spawnWithScale(e, Duration.seconds(0.5), Interpolators.BOUNCE.EASE_OUT());

        }, Duration.seconds(1));
    }

    @Override
    protected void initPhysics() {
        onCollisionBegin(EntityType.BULLET, EntityType.ASTEROID, (bullet, asteroid) -> {
            spawn("scoreText", new SpawnData(asteroid.getX(), asteroid.getY()).put("text", "+100"));

            Point2D explosionSpawnPoint = asteroid.getCenter().subtract(64, 64);
            spawn("explosion", explosionSpawnPoint);
            asteroid.removeFromWorld();

            bullet.removeFromWorld();
            inc("score", +100);
        });

        onCollisionBegin(EntityType.PLAYER, EntityType.ASTEROID, (player, asteroid) -> {
            Point2D explosionSpawnPoint = asteroid.getCenter().subtract(64, 64);
            spawn("explosion", explosionSpawnPoint);
            asteroid.removeFromWorld();

            player.setPosition(getAppWidth() / 2, getAppHeight() / 2);
            inc("lives", -1);
        });
    }

    @Override
    protected void initUI() {
        var text = getUIFactory().newText("", 24);
        text.textProperty().bind(getip("score").asString("Score: [%d]"));

        getGameState().addListener("score", (prev, now) -> {
            animationBuilder()
                .interpolator(Interpolators.BOUNCE.EASE_OUT())
                .repeat(2)
                .autoReverse(true)
                .scale(text)
                .from(new Point2D(1, 1))
                .to(new Point2D(1.2, 1.2))
                .buildAndPlay();
        });

        addUINode(text, 20, 50);
        addVarText("lives", 50, 70);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

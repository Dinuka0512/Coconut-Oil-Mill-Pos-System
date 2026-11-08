package com.dev09.kumaracoconutoil.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.input.InputEvent;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Monitors user activity across all application windows and runs a callback after
 * a period of inactivity. Intended for auto-logout.
 */
public final class IdleMonitor {

    private static final Duration DEFAULT_TIMEOUT = Duration.minutes(1);

    private static Timeline timeline;
    private static Runnable onTimeout;
    private static boolean running;
    private static final Map<Scene, EventHandler<? super InputEvent>> sceneFilters = new IdentityHashMap<>();
    private static final EventHandler<? super InputEvent> ACTIVITY_HANDLER = e -> reset();
    private static final ListChangeListener<Window> WINDOW_LISTENER = change -> {
        while (change.next()) {
            if (change.wasAdded()) {
                for (Window w : change.getAddedSubList()) {
                    attachToWindow(w);
                }
            }
            if (change.wasRemoved()) {
                for (Window w : change.getRemoved()) {
                    detachFromWindow(w);
                }
            }
        }
    };

    private IdleMonitor() {
    }

    public static void start(Runnable timeoutAction) {
        start(DEFAULT_TIMEOUT, timeoutAction);
    }

    public static void start(Duration timeout, Runnable timeoutAction) {
        stop();
        onTimeout = timeoutAction;
        timeline = new Timeline(new KeyFrame(timeout, e -> {
            if (onTimeout != null) {
                onTimeout.run();
            }
        }));
        timeline.setCycleCount(1);
        running = true;

        for (Window w : Window.getWindows()) {
            attachToWindow(w);
        }
        Window.getWindows().addListener(WINDOW_LISTENER);
        timeline.playFromStart();
    }

    public static void stop() {
        running = false;
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        try {
            Window.getWindows().removeListener(WINDOW_LISTENER);
        } catch (Exception ignored) {
        }
        for (Map.Entry<Scene, EventHandler<? super InputEvent>> entry : sceneFilters.entrySet()) {
            try {
                entry.getKey().removeEventFilter(InputEvent.ANY, entry.getValue());
            } catch (Exception ignored) {
            }
        }
        sceneFilters.clear();
        onTimeout = null;
    }

    public static void reset() {
        if (running && timeline != null) {
            timeline.playFromStart();
        }
    }

    private static void attachToWindow(Window window) {
        if (window == null) return;
        Scene scene = window.getScene();
        if (scene != null) {
            attachToScene(scene);
        }
        window.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) detachFromScene(oldScene);
            if (newScene != null) attachToScene(newScene);
        });
    }

    private static void detachFromWindow(Window window) {
        if (window == null) return;
        Scene scene = window.getScene();
        if (scene != null) {
            detachFromScene(scene);
        }
    }

    private static void attachToScene(Scene scene) {
        if (scene == null || sceneFilters.containsKey(scene)) return;
        scene.addEventFilter(InputEvent.ANY, ACTIVITY_HANDLER);
        sceneFilters.put(scene, ACTIVITY_HANDLER);
    }

    private static void detachFromScene(Scene scene) {
        if (scene == null) return;
        EventHandler<? super InputEvent> filter = sceneFilters.remove(scene);
        if (filter != null) {
            try {
                scene.removeEventFilter(InputEvent.ANY, filter);
            } catch (Exception ignored) {
            }
        }
    }
}

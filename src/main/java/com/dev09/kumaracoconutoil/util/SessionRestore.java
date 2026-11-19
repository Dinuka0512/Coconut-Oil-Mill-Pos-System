package com.dev09.kumaracoconutoil.util;

import com.dev09.kumaracoconutoil.controller.SimplePOSController;
import com.dev09.kumaracoconutoil.dto.UserDto;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * In-memory "resume point" for an auto-logout.
 *
 * <p>At idle logout we remember which screen the user was filling, the typed values,
 * and any tracked pop-up window (for example the invoice window). When somebody logs
 * in again the snapshot is only handed back to the exact account that owned it; every
 * other account simply starts at its normal home screen.</p>
 *
 * <p>Everything is process local and is discarded on manual logout, on a different
 * login, or when the application exits - no disk I/O and no long-lived scene graph.</p>
 */
public final class SessionRestore {

    public static final String TRACK_FXML = "sessionRestore.fxml";
    public static final String TRACK_CONTROLLER = "sessionRestore.controller";

    public record CartItem(String code, String description, double qty, double unitPrice, double total) {
    }

    /** Invoice-specific state: the cart cannot be rebuilt from text fields alone. */
    public record PosDraft(String customerId, String productCode, String productDisplay,
                           String customerEditor, String productEditor,
                           double selectedTierQty, String qty, String unitPrice,
                           List<CartItem> cart) {
        public PosDraft {
            cart = List.copyOf(cart);
        }
    }

    public record WindowState(String fxmlPath, String title, double width, double height,
                              double x, double y, Map<String, String> fields, PosDraft pos) {
    }

    public record Snapshot(String ownerId, String ownerRole, String homeFxml,
                           String subViewFxml, Map<String, String> subViewFields,
                           List<WindowState> windows, long savedAt) {
    }

    private static Snapshot pending;

    private SessionRestore() {
    }

    /** The correct landing screen for a role (also used for normal logins). */
    public static String homeFxmlFor(String role) {
        return RoleUtil.isCashier(role) ? "/view/clientDashboard.fxml" : "/view/dashboard.fxml";
    }

    /** Mark a non-modal pop-up so its values can be captured before it is closed. */
    public static void track(Stage stage, String fxmlPath, Object controller) {
        stage.getProperties().put(TRACK_FXML, fxmlPath);
        stage.getProperties().put(TRACK_CONTROLLER, controller);
        stage.setOnHidden(e -> stage.getProperties().remove(TRACK_CONTROLLER));
    }

    public static void save(Snapshot snapshot) {
        pending = snapshot;
    }

    public static void clear() {
        pending = null;
    }

    /**
     * Returns the pending work only to the account that created it. A different
     * account (even with the same role) discards it and starts fresh.
     */
    public static Snapshot consumeFor(UserDto user) {
        Snapshot snapshot = pending;
        pending = null;
        if (snapshot == null || user == null) return null;
        String savedRole = RoleUtil.normalize(snapshot.ownerRole());
        boolean sameAccount = snapshot.ownerId() != null
                && snapshot.ownerId().equals(user.getId())
                && savedRole != null
                && savedRole.equalsIgnoreCase(RoleUtil.normalize(user.getRole()));
        return sameAccount ? snapshot : null;
    }

    public static Snapshot capture(UserDto owner, String homeFxml, String subViewFxml, Parent subViewRoot) {
        if (owner == null) return null;
        Map<String, String> fields = subViewRoot == null ? Map.of() : FormState.capture(subViewRoot);
        return new Snapshot(
                owner.getId(),
                RoleUtil.normalize(owner.getRole()),
                homeFxml,
                subViewFxml,
                fields,
                captureTrackedWindows(),
                System.currentTimeMillis()
        );
    }

    /** Reopen the pop-ups that were closed by idle logout and refill them. */
    public static void reopen(List<WindowState> windows) {
        if (windows == null) return;
        for (WindowState window : windows) {
            try {
                FXMLLoader loader = new FXMLLoader(SessionRestore.class.getResource(window.fxmlPath()));
                Parent root = loader.load();
                Object controller = loader.getController();

                if (controller instanceof SimplePOSController pos && window.pos() != null) {
                    pos.applyDraft(window.pos());
                } else {
                    FormState.restore(root, window.fields());
                }

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                if (window.title() != null) stage.setTitle(window.title());
                stage.setResizable(false);
                if (window.width() > 0) stage.setWidth(window.width());
                if (window.height() > 0) stage.setHeight(window.height());
                if (Double.isFinite(window.x()) && Double.isFinite(window.y())) {
                    stage.setX(window.x());
                    stage.setY(window.y());
                }
                track(stage, window.fxmlPath(), controller);
                stage.show();
            } catch (Exception e) {
                // Never block login because a saved pop-up could not be rebuilt.
                e.printStackTrace();
            }
        }
    }

    private static List<WindowState> captureTrackedWindows() {
        List<WindowState> result = new ArrayList<>();
        for (Window window : new ArrayList<>(Window.getWindows())) {
            if (!(window instanceof Stage stage)) continue;
            Object path = stage.getProperties().get(TRACK_FXML);
            if (!(path instanceof String fxmlPath)) continue;
            try {
                Parent root = stage.getScene() == null ? null : stage.getScene().getRoot();
                Object controller = stage.getProperties().get(TRACK_CONTROLLER);
                PosDraft pos = controller instanceof SimplePOSController simplePos
                        ? simplePos.captureDraft() : null;
                result.add(new WindowState(
                        fxmlPath,
                        stage.getTitle(),
                        stage.getWidth(),
                        stage.getHeight(),
                        stage.getX(),
                        stage.getY(),
                        FormState.capture(root),
                        pos
                ));
            } catch (Exception ignored) {
                // A pop-up that cannot be captured is simply not restored.
            }
        }
        return result;
    }
}

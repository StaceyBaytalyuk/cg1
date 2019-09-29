package lab1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;

public class Controller implements Initializable {
    private double minX = -50;
    private double maxX = 50;
    private double minY = -50;
    private double maxY = 50;
    private double scale = 1;
    private String language = "eng";

    @FXML private Pane centerPane;
    @FXML private Canvas canvas;
    @FXML private TextField aField;
    @FXML private TextField bField;
    @FXML private TextField scaleField;
    @FXML private Button drawButton;
    @FXML private Text scaleText;
    @FXML private Menu fileMenu;
    @FXML private MenuItem closeMenu;
    @FXML private Menu settingsMenu = new Menu();
    @FXML private Menu languageMenu;

    public void close() {
        Platform.exit();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        canvas.widthProperty().bind(centerPane.widthProperty());
        canvas.heightProperty().bind(centerPane.heightProperty());
        canvas.widthProperty().addListener(e->draw());
        canvas.heightProperty().addListener(e->draw());
    }

    public void draw() {
        if ( checkBounds() ) {
            drawGraph();
        } else {
            errorDialog();
        }
    }

    private void drawAxis(Color color, GraphicsContext gc) {
        gc.setStroke(color);
        gc.strokeLine(toScreenX(minX), toScreenY(0), toScreenX(maxX), toScreenY(0));
        gc.strokeLine(toScreenX(0),toScreenY(maxY),toScreenX(0),toScreenY(minY));
    }

    private void drawGraph() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITESMOKE);
        gc.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
        drawAxis(Color.BLACK, gc);

        List<Point> points = tabulation();
        int i = 0;
        while ( points.get(i) == null ) {
            i++;
        }
        gc.setStroke(Color.RED);
        gc.beginPath();
        gc.moveTo(toScreenX(points.get(i).getX()), toScreenY(points.get(i).getY()));
        i++;

        for (; i<points.size(); i++) {
            try {
                if ( points.get(i) != null ) {
                    gc.lineTo(toScreenX(points.get(i).getX()*scale), toScreenY(points.get(i).getY()*scale));
                    gc.stroke();
                } else {
                    gc.beginPath();
                }
            } catch (NullPointerException ignored) {}
        }
    }

    private boolean checkBounds() {
        try {
            double a = Double.parseDouble(aField.getText());
            double b = Double.parseDouble(bField.getText());
            double scale1 = Double.parseDouble(scaleField.getText());
            if ( a > b ) {
                double temp = a;
                a = b;
                b = temp;
            }

            double EPS = 0.000000001;
            if ( (b-a) <= EPS) {
                return false;
            } else {
                minX = a;
                maxX = b;
                scale = scale1;
                return true;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private List<Point> tabulation() {
        return IntStream
                .range(0, (int) canvas.getWidth())
                .mapToDouble(this::toWorldX)
                .mapToObj(x-> {
                    Double f = f(x);
                    if ( f != null ) return new Point(x, f);
                    else return null;
                }).collect(Collectors.toList());
    }

    private int toScreenX(double x) {
        return (int) (canvas.getWidth() * (x - minX) / (maxX - minX));
    }

    private int toScreenY(double y) {
        return (int) (canvas.getHeight() * (1 - (y - minY) / (maxY - minY)));
    }

    private double toWorldX(int xs) {
        return 1.0 * xs / canvas.getWidth() * (maxX - minX) + minX;
    }

    private double toWorldY(int ys) {
        return (1.0 * ys - canvas.getHeight()) / (-canvas.getHeight()) * (maxY - minY) + minY;
    }

    private Double f(double x) {
        if ( x != -1 ) {
            return cos(x*x) / sqrt(x+1);
        } else {
            return null;
        }
    }

    public void onEnglish() {
        language = "eng";
        fileMenu.setText("File");
        closeMenu.setText("Close");
        settingsMenu.setText("Settings");
        languageMenu.setText("Language");
        drawButton.setText("Draw");
        scaleText.setText("      scale:");
    }

    public void onRussian() {
        language = "rus";
        fileMenu.setText("Файл");
        closeMenu.setText("Закрыть");
        settingsMenu.setText("Настройки");
        languageMenu.setText("Язык");
        drawButton.setText("Рисовать");
        scaleText.setText("      масштаб:");
    }

    private void errorDialog() {
        if ( language.equals("eng") ) {
            showAlert("Error", "Incorrect input data");
        } else if ( language.equals("rus") ) {
            showAlert("Ошибка", "Неправильно введены данные");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }
}
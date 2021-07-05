import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class UIMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MySystemTray.getInstance().listen(primaryStage);
        int width = 500;
        int height = 300;
        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.getIcons().add(new Image("/eth.png"));
        primaryStage.setAlwaysOnTop(true);//一直悬浮
        //primaryStage.setResizable(true);//不准拖动改变大小
        primaryStage.setTitle("串口工具(v2021.07.05)");//标题
        primaryStage.setScene(new Scene(root, width, height));//宽高
        Rectangle2D rectangle2D = Screen.getPrimary().getBounds();
        primaryStage.setX(rectangle2D.getWidth() - width - 10);//X轴
        primaryStage.setY(0);//Y轴
        primaryStage.show();
    }
}

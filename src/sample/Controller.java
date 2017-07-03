package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.DirectoryChooserBuilder;
import javafx.stage.Stage;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class Controller {
    static public Stage stage;



    @FXML
    private Button btn_docPath,btn_query,btn_indexPath,btn_index;

    @FXML
    private TextField txt_docPath,txt_indexPath,txt_keyword;

    @FXML
    private TableView<Index> table;
    @FXML
    private TableColumn<Index, String> col_term;
    @FXML
    private TableColumn<Index, String> col_path;

    private File docPath, indexPath;

    @FXML
    private void handleDocPathClick(ActionEvent event) {
        DirectoryChooserBuilder builder = DirectoryChooserBuilder.create();
        builder.title("Choose Document Path");
        String cwd = System.getProperty("user.dir");
        File file = new File(cwd);
        builder.initialDirectory(file);
        DirectoryChooser chooser = builder.build();
        File chosenDir = chooser.showDialog(stage);
        if (chosenDir != null) {
            docPath = chosenDir;
            System.out.println(chosenDir.getAbsolutePath());
            this.txt_docPath.setText(chosenDir.getAbsolutePath());
        } else {
            System.out.print("no directory chosen");
        }
    }

    @FXML
    private void handleIndexPathClick(ActionEvent event) {
        DirectoryChooserBuilder builder = DirectoryChooserBuilder.create();
        builder.title("Choose Document Path");
        String cwd = System.getProperty("user.dir");
        File file = new File(cwd);
        builder.initialDirectory(file);
        DirectoryChooser chooser = builder.build();
        File chosenDir = chooser.showDialog(stage);
        if (chosenDir != null) {
            System.out.println(chosenDir.getAbsolutePath());
            indexPath = chosenDir;
            this.txt_indexPath.setText(chosenDir.getAbsolutePath());
        } else {
            System.out.print("no directory chosen");
        }
    }

    @FXML
    private void handleQuery(ActionEvent event) {

        String str_keyword = txt_keyword.getText();
        if (str_keyword == null || str_keyword.trim() == "") {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("Doc path or index path is empty!");
            alert.setContentText("Go back modify!");
            alert.showAndWait();
            return;
        }

        if (indexPath == null || docPath == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("Doc path or index path is empty!");
            alert.setContentText("Go back modify!");
            alert.showAndWait();
            return;
        }

        Service srv = new Service();
        String result;
        try {
            result = srv.search(indexPath.getAbsolutePath(),str_keyword);
        } catch (JSONException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(e.getMessage());
            alert.setContentText(e.getLocalizedMessage());
            alert.showAndWait();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(e.getMessage());
            alert.setContentText(e.getLocalizedMessage());
            alert.showAndWait();
            return;
        } catch (InvalidTokenOffsetsException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(e.getMessage());
            alert.setContentText(e.getLocalizedMessage());
            alert.showAndWait();
            return;
        }
        showResult(result);
        System.out.println(result);

    }

    @FXML
    private void handleIndex(ActionEvent event) {
        if (indexPath == null || docPath == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("Doc path or index path is empty!");
            alert.setContentText("Go back modify!");
            alert.showAndWait();
            return;
        }
        Service srv = new Service();
        String result;
        try {
            result = srv.createIndex(docPath.getAbsolutePath()+"\\",indexPath.getAbsolutePath()+"\\");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(e.getMessage());
            alert.setContentText(e.getLocalizedMessage());
            alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText("Index Complete!");
        alert.showAndWait();
        System.out.println(result);
    }

    private void showResult(String str_result) {
        JSONObject json;
        JSONArray jsonArray;
        try {
            json = new JSONObject(str_result);
            jsonArray = json.getJSONArray("results");
            int size = json.getInt("count");
            ObservableList<Index> data = FXCollections.observableArrayList();
            for (int i = 0; i < size; i++) {

                JSONObject obj = jsonArray.getJSONObject(i);
                Index idx = new Index(obj.getString("body"),obj.getString("path"));
                data.add(idx);
            }

            col_term.setCellValueFactory(
                    new PropertyValueFactory<Index,String>("term")
            );
            col_path.setCellValueFactory(
                    new PropertyValueFactory<Index,String>("path")
            );
            table.setItems(data);

        } catch (JSONException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(e.getMessage());
            alert.setContentText(e.getLocalizedMessage());
            alert.showAndWait();
            return;
        }

    }

}

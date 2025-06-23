package controller;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.BorrowDevice;
import model.BorrowDeviceDetail;
import model.BorrowRoom;
import repository.BorrowDeviceRepository;
import utils.ScannerUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class BorrowDeviceListController implements Initializable {
    @FXML private TableView<BorrowDevice> tblBorrowList;
    @FXML private TableColumn<BorrowDevice,String> colId, colName, colDate, colHour, colStatus, colBorrowQuantity;
    @FXML private TableColumn<BorrowDevice, Void> colAction;

    private BorrowDeviceRepository borrowDeviceRepository = new BorrowDeviceRepository();
    private BorrowDeviceController borrowDeviceController = new BorrowDeviceController();

    public void initialize(URL location, ResourceBundle resources){
        loadBorrowDevice();
    }

    private void loadBorrowDevice(){
        List<BorrowDevice> datas = borrowDeviceRepository.getAllBorrowDevices();
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBorrowDeviceDetail().getDevice().getDeviceName()));
        colDate.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        colHour.setCellValueFactory(cellData -> {
            BorrowDevice br = cellData.getValue();
            String hourRange = br.getStartPeriod() + " - " + br.getEndPeriod();
            return new SimpleStringProperty(hourRange);
        });
        colStatus.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBorrowStatus().toString()));
        colBorrowQuantity.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(String.valueOf(cellData.getValue().getBorrowDeviceDetail().getQuantity()))
        );
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnCancel = new Button("Hủy yêu cầu");
            private final Button btnDetail = new Button("Chi tiết");
            private final Button btnReason = new Button("Lý do");
            private final Button btnReturn = new Button("Trả thiết bị");

            {
                btnCancel.setOnAction(event -> {
                    BorrowDevice br = getTableView().getItems().get(getIndex());

                    if(ScannerUtils.showConfirm("Xác nhận", "Bạn có chắc chắn muốn hủy yêu cầu không?")){
                        Boolean success = borrowDeviceRepository.cancle(br.getId());
                        if(success){
                            ScannerUtils.showInfo("Thông báo", "Hủy yêu cầu thành công!");
                            loadBorrowDevice();
                        } else{
                            ScannerUtils.showError("Thông báo", "Hủy yêu cầu thất bại!");
                        }
                    }

                });

                btnDetail.setOnAction(event -> {
                    BorrowDevice br = getTableView().getItems().get(getIndex());

                    showBorrowDeviceDetail(br);
                });

                btnReason.setOnAction(event -> {
                    BorrowDevice br = getTableView().getItems().get(getIndex());
                    String reason = br.getRejectReason();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Lý do từ chối");
                    alert.setHeaderText("Yêu cầu bị từ chối");
                    alert.setContentText(reason != null && !reason.isEmpty() ? reason : "Không có lý do cụ thể.");
                    alert.showAndWait();
                });

                btnReturn.setOnAction(event -> {
                    BorrowDevice request = getTableView().getItems().get(getIndex());

                    showReturnDialog(request);
                });

                btnCancel.getStyleClass().add("btn-cancel");
                btnDetail.getStyleClass().add("btn-detail");
                btnReason.getStyleClass().add("btn-reason");
                btnReturn.getStyleClass().add("btn-return");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                BorrowDevice br = getTableView().getItems().get(getIndex());
                switch (br.getBorrowStatus()) {
                    case PENDING -> setGraphic(btnCancel);
                    case APPROVED -> {
                        HBox actionBox = new HBox(5, btnDetail, btnReturn);
                        setGraphic(actionBox);
                    }
                    case REJECTED -> setGraphic(btnReason);
                    default -> setGraphic(null);
                }
            }
        });

        tblBorrowList.setItems(FXCollections.observableArrayList(datas));
        tblBorrowList.setSelectionModel(null);
    }

    private void showReturnDialog(BorrowDevice request) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Trả thiết bị");
        dialog.getDialogPane().setMinWidth(400);
        dialog.getDialogPane().setMinHeight(300);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f9f9f9;");

        Label nameLabel = new Label("🔧 Thiết bị: " + request.getBorrowDeviceDetail().getDevice().getDeviceName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label quantityLabel = new Label("📦 Số lượng đã mượn: " + request.getBorrowDeviceDetail().getQuantity());
        quantityLabel.setStyle("-fx-font-size: 13px;");

        Label returnLabel = new Label("📥 Số lượng trả:");
        Spinner<Integer> spinner = new Spinner<>(1, request.getBorrowDeviceDetail().getQuantity(), request.getBorrowDeviceDetail().getQuantity());
        spinner.setEditable(true);
        spinner.setMaxWidth(100.0);

        Label noteLabel = new Label("📝 Ghi chú khi trả:");
        TextArea noteArea = new TextArea();
        noteArea.setPromptText("");
        noteArea.setWrapText(true);
        noteArea.setPrefRowCount(3);

        content.getChildren().addAll(
                nameLabel,
                quantityLabel,
                returnLabel, spinner,
                noteLabel, noteArea
        );

        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        // Xử lý khi xác nhận
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if(ScannerUtils.showConfirm("Xác nhận", "Bạn có chắc chắn muốn trả phòng không?")){
                    int returnedQty = spinner.getValue();
                    String note = noteArea.getText().trim();


                    Boolean success = borrowDeviceRepository.updateDeviceReturn(request.getBorrowDeviceDetail().getId(), returnedQty, note);
                    borrowDeviceRepository.updateBorrowDeviceStatus(request.getId());
                    int brokenQunatity = request.getBorrowDeviceDetail().getQuantity() - returnedQty;
                    borrowDeviceRepository.updateDeviceAvailableQuantity(request.getBorrowDeviceDetail().getDevice().getId(), brokenQunatity);
                    if(success) {
                        ScannerUtils.showInfo("Thông báo", "Trả thiết bị thành công");
                        dialog.close();
                        loadBorrowDevice();
                        borrowDeviceController.loadPage(0);
                    } else {
                        ScannerUtils.showError("Thông báo", "Trả thiết bị không thành công");
                    }
                }
            }
            return null;
        });

        dialog.showAndWait();
    }



    public void showBorrowDeviceDetail(BorrowDevice request) {
        Stage stage = new Stage();
        stage.setTitle("Chi tiết yêu cầu mượn thiết bị");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TextFlow deviceFlow = new TextFlow(
                new Text("Thiết bị: ") {{ setStyle("-fx-font-weight: bold;"); }},
                new Text(request.getBorrowDeviceDetail().getDevice().getDeviceName())
        );

        TextFlow quantityFlow = new TextFlow(
                new Text("Số lượng mượn: ") {{ setStyle("-fx-font-weight: bold;"); }},
                new Text(String.valueOf(request.getBorrowDeviceDetail().getQuantity()))
        );

        TextFlow dateFlow = new TextFlow(
                new Text("Ngày mượn: ") {{ setStyle("-fx-font-weight: bold;"); }},
                new Text(request.getBorrowDate().toString())
        );

        TextFlow timeFlow = new TextFlow(
                new Text("Thời gian: ") {{ setStyle("-fx-font-weight: bold;"); }},
                new Text(request.getStartPeriod() + " - " + request.getEndPeriod())
        );

        TextFlow statusFlow = new TextFlow(
                new Text("Trạng thái: ") {{ setStyle("-fx-font-weight: bold;"); }},
                new Text(request.getBorrowStatus().toString())
        );

        TextFlow noteFlow = new TextFlow(
                new Text("Ghi chú: ") {{ setStyle("-fx-font-weight: bold;"); }},
                new Text(request.getBorrowReason() == null || request.getBorrowReason().isBlank() ? "Không có" : request.getBorrowReason())
        );

        Button btnExport = new Button("Xuất file");
        btnExport.getStyleClass().add("exportButton");

        VBox.setMargin(btnExport, new Insets(10, 0, 0, 0));
        btnExport.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Lưu file PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss");
            String timestamp = LocalDateTime.now().format(formatter);
            fileChooser.setInitialFileName("GiayMuonThietBi_" + timestamp + ".pdf");
            File file = fileChooser.showSaveDialog(stage);
            try {
                exportToFile(request, file);
                ScannerUtils.showInfo("Thông báo", "Xuất file thành công!");
                stage.close();
            } catch (IOException e) {
                e.printStackTrace();
                ScannerUtils.showError("Thông báo", "Xuất file thất bại!");
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            }
        });

        root.getChildren().addAll(deviceFlow, quantityFlow, dateFlow, timeFlow, statusFlow, noteFlow, btnExport);

        Scene scene = new Scene(root, 400, Region.USE_COMPUTED_SIZE);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public void exportToFile(BorrowDevice request, File file) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        writer.setPageEvent(new BorrowDeviceListController.BackgroundImageEvent("resource/img/logo.png"));
        document.open();

        BaseFont baseFont = BaseFont.createFont("C:\\Windows\\Fonts\\times.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font fontNormal = new Font(baseFont, 12);
        Font fontBold = new Font(baseFont, 12, Font.BOLD);
        System.out.println("Tên thiết bị: " + request.getBorrowDeviceDetail().getDevice().getDeviceName());

        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 1});

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setVerticalAlignment(Element.ALIGN_TOP);
        Paragraph line1 = new Paragraph("TRƯỜNG ĐẠI HỌC GIAO THÔNG VẬN TẢI", fontBold);
        line1.setAlignment(Element.ALIGN_CENTER);
        Paragraph line2 = new Paragraph("PHÂN HIỆU TẠI TP. HỒ CHÍ MINH", fontBold);
        line2.setAlignment(Element.ALIGN_CENTER);
        leftCell.addElement(line1);
        leftCell.addElement(line2);


        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setVerticalAlignment(Element.ALIGN_TOP);
        Paragraph line3 = new Paragraph("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM", fontBold);
        line3.setAlignment(Element.ALIGN_CENTER);
        Paragraph line4 = new Paragraph("Độc lập - Tự do - Hạnh phúc", fontBold);
        line4.setAlignment(Element.ALIGN_CENTER);

        rightCell.addElement(line3);
        rightCell.addElement(line4);

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);

        document.add(headerTable);


        LocalDate now = LocalDate.now();
        Paragraph date = new Paragraph(
                "Tp. Hồ Chí Minh, ngày " + now.getDayOfMonth()
                        + " tháng " + now.getMonthValue()
                        + " năm " + now.getYear(),
                fontNormal
        );
        date.setAlignment(Element.ALIGN_RIGHT);
        document.add(date);

        Paragraph title = new Paragraph("GIẤY ĐĂNG KÝ SỬ DỤNG THIẾT BỊ", new Font(baseFont, 14, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph("Kính gửi: Phòng Đào tạo", fontNormal));
        document.add(new Paragraph("Họ và tên sinh viên: " + request.getBorrower().getFullname(), fontNormal));
        document.add(new Paragraph("Lý do mượn thiết bị: " + request.getBorrowReason(), fontNormal));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        String[] headers = {"STT", "Lớp", "NGÀY", "TIẾT", "THIẾT BỊ", "SỐ LƯỢNG", "GHI CHÚ"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fontBold));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        table.addCell("1");
        if(request.getBorrower().getClasses() != null){
            table.addCell(request.getBorrower().getClasses());
        } else if(request.getBorrower().getDepartment() != null){
            table.addCell(request.getBorrower().getDepartment());
        }
        table.addCell(request.getBorrowDate().toString());
        table.addCell(request.getStartPeriod() + "-" + request.getEndPeriod());
        table.addCell(new PdfPCell(new Phrase(request.getBorrowDeviceDetail().getDevice().getDeviceName(), fontNormal)));
        table.addCell(String.valueOf(request.getBorrowDeviceDetail().getQuantity()));
        table.addCell("");

        document.add(table);

        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        PdfPTable signTable = new PdfPTable(2);
        signTable.setWidthPercentage(100);
        signTable.setSpacingBefore(200f);
        signTable.setWidths(new int[]{1, 1});

        PdfPCell cell1 = new PdfPCell(new Phrase("NGƯỜI MƯỢN", fontBold));
        PdfPCell cell2 = new PdfPCell(new Phrase("NGƯỜI DUYỆT", fontBold));
        PdfPCell cell3 = new PdfPCell(new Phrase("(Ký và ghi rõ họ tên)", fontNormal));
        PdfPCell cell4 = new PdfPCell(new Phrase("(Ký và ghi rõ họ tên)", fontNormal));

        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell4.setHorizontalAlignment(Element.ALIGN_CENTER);

        cell1.setBorder(Rectangle.NO_BORDER);
        cell2.setBorder(Rectangle.NO_BORDER);
        cell3.setBorder(Rectangle.NO_BORDER);
        cell4.setBorder(Rectangle.NO_BORDER);

        signTable.addCell(cell1);
        signTable.addCell(cell2);
        signTable.addCell(cell3);
        signTable.addCell(cell4);

        document.add(signTable);
        document.close();
    }

    public class BackgroundImageEvent extends PdfPageEventHelper {
        private Image backgroundImage;

        public BackgroundImageEvent(String imagePath) throws IOException, BadElementException {
            this.backgroundImage = Image.getInstance(imagePath);
            this.backgroundImage.setAbsolutePosition(100, 300);
            this.backgroundImage.scaleAbsolute(400, 400);
            this.backgroundImage.setAlignment(Image.UNDERLYING);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte canvas = writer.getDirectContentUnder();
                canvas.addImage(backgroundImage);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
    }
}

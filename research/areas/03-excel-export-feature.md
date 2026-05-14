# Research Area: Excel Export Feature

## Status: 🟢 Complete

## What I Need To Learn
- [x] Which files were modified for excel export
- [x] What library is used for excel export
- [x] What data is exported
- [x] How the export functionality works

## Source
**Commit**: `bb95d02` - feat: export ListView data to Excel  
**Date**: Mon Mar 2 21:34:07 2026 +0100  
**Author**: Aymen Ben Salem

## Files Changed

| File | Change Type | Description |
|------|-------------|-------------|
| `pom.xml` | Modified | Added Apache POI dependencies |
| `module-info.java` | Modified | Added Apache POI modules |
| `UserListController.java` | Modified | Added export functionality |
| `user-list.fxml` | Modified | Added export button |

## Detailed Changes

### 1. pom.xml (Lines 142-152)
**Add dependencies:**
```xml
<!-- Apache POI for Excel Export -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.5</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

### 2. module-info.java
**Add requires:**
```java
requires org.apache.poi.poi;
requires org.apache.poi.ooxml;
```

### 3. UserListController.java Changes

**New Imports:**
```java
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
```

**New Field:**
```java
@FXML
private Button exportButton;
```

**New Method: `handleExportExcel()`**

Complete implementation:
```java
@FXML
private void handleExportExcel() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Enregistrer la liste des utilisateurs");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
    fileChooser.setInitialFileName("Liste_Utilisateurs.xlsx");

    File file = fileChooser.showSaveDialog(userListView.getScene().getWindow());

    if (file != null) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Utilisateurs");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = { "ID", "Nom", "Prénom", "Email", "CIN", "Téléphone", "Rôle", "Adresse" };

            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Fill data
            int rowNum = 1;
            for (User user : userList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getIdUser());
                row.createCell(1).setCellValue(user.getNom());
                row.createCell(2).setCellValue(user.getPrenom());
                row.createCell(3).setCellValue(user.getEmail());
                row.createCell(4).setCellValue(user.getCin());
                row.createCell(5).setCellValue(user.getTelephone());
                row.createCell(6).setCellValue(user.getRole() != null ? user.getRole().getDisplayName() : "");
                row.createCell(7).setCellValue(user.getAdresse());
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }

            NavigationUtil.showSuccess("Export réussi",
                    "La liste des utilisateurs a été exportée avec succès vers Excel.");
            NotificationManager.addNotification("Utilisateurs exportés vers " + file.getName());

        } catch (IOException e) {
            e.printStackTrace();
            NavigationUtil.showError("Erreur d'export",
                    "Une erreur est survenue lors de la création du fichier Excel.");
        }
    }
}
```

### 4. user-list.fxml Changes

**Add export button** after refresh button:

```xml
<Button fx:id="exportButton" text="📊 Exporter Excel" styleClass="secondary-btn" onAction="#handleExportExcel"/>
```

**Location**: In the HBox containing action buttons (refresh, audit, add user).

## Code Patterns Observed
- Uses try-with-resources for proper resource management
- Creates .xlsx format (XSSFWorkbook) - modern Excel format
- Auto-sizes columns after data insertion
- Shows success/error notifications via NavigationUtil
- French UI text and messages

## Relevance to Implementation
This feature allows administrators to export the user list to Excel format for reporting, backup, or data analysis purposes. It's implemented in the User Management module.

## Dependencies Required
- Apache POI 5.2.5 (poi + poi-ooxml)
- Creates .xlsx files (Office Open XML format)

## Exported Data Columns
1. ID - User ID
2. Nom - Last name
3. Prénom - First name
4. Email - Email address
5. CIN - National ID
6. Téléphone - Phone number
7. Rôle - User role (display name)
8. Adresse - Address

## Testing Checklist
- [ ] Export button visible in user list
- [ ] File chooser dialog opens
- [ ] Excel file created with correct extension
- [ ] All user data exported correctly
- [ ] Headers are bold
- [ ] Columns auto-sized
- [ ] Success notification shown
- [ ] Error handling works for invalid paths

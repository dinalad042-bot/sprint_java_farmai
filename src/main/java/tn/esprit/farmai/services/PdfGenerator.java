package tn.esprit.farmai.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public class PdfGenerator {

    /**
     * Méthode statique pour générer un PDF à partir d'une liste d'objets.
     */
    public static <T> void generatePdf(String fileName, String title, List<T> data, String[] headers, Function<T, String>[] extractors) {
        try {
            // Initialisation du fichier
            PdfWriter writer = new PdfWriter(new File(fileName));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Ajout du Titre
            document.add(new Paragraph(title).setBold().setFontSize(18));
            document.add(new Paragraph("Date du rapport : " + java.time.LocalDateTime.now().toString()));
            document.add(new Paragraph("\n"));

            // Création du tableau (largeur 100%)
            Table table = new Table(UnitValue.createPercentArray(headers.length)).useAllAvailableWidth();

            // Ajout des entêtes (Headers)
            for (String header : headers) {
                table.addHeaderCell(new Paragraph(header).setBold());
            }

            // Remplissage des données avec les extracteurs
            for (T item : data) {
                for (Function<T, String> extractor : extractors) {
                    String value = (item != null) ? extractor.apply(item) : "";
                    table.addCell(new Paragraph(value != null ? value : ""));
                }
            }

            document.add(table);
            document.close();
            System.out.println("PDF généré avec succès dans : " + fileName);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la génération du PDF : " + e.getMessage());
        }
    }
}
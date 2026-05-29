package ma.karflow.shared.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import ma.karflow.feature.rental.dto.RentalResponse;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Utilitaire de génération PDF avec OpenPDF.
 * Les méthodes spécifiques (facture, contrat) seront ajoutées dans F-08 et F-16.
 */
@Slf4j
@Component
public class PdfGenerator {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(52, 73, 94));
    private static final Font LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.GRAY);
    private static final Font VALUE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Génère un PDF simple avec un titre et un tableau de données.
     * Sert de base — les features billing et contract construiront dessus.
     */
    public byte[] generateTablePdf(String title, List<String> headers, List<List<String>> rows) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Titre
            Paragraph titleParagraph = new Paragraph(title, TITLE_FONT);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            titleParagraph.setSpacingAfter(20);
            document.add(titleParagraph);

            // Tableau
            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);

            // En-têtes
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
                cell.setBackgroundColor(new Color(52, 73, 94));
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Lignes
            for (List<String> row : rows) {
                for (String value : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", BODY_FONT));
                    cell.setPadding(6);
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF", e);
            throw new RuntimeException("Erreur de génération PDF", e);
        }
    }

    public byte[] generateContractPdf(RentalResponse rental) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 60, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            // En-tête
            Paragraph title = new Paragraph("CONTRAT DE LOCATION DE VÉHICULE", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);

            Paragraph refLine = new Paragraph("Réf. : " + rental.id(), LABEL_FONT);
            refLine.setAlignment(Element.ALIGN_CENTER);
            refLine.setSpacingAfter(20);
            document.add(refLine);

            addSeparator(document);

            // Informations véhicule
            document.add(sectionTitle("VÉHICULE"));
            PdfPTable vehicleTable = infoTable();
            addInfoRow(vehicleTable, "Modèle", rental.vehicleModelName() != null ? rental.vehicleModelName() : "-");
            addInfoRow(vehicleTable, "Immatriculation", rental.vehicleLicensePlate() != null ? rental.vehicleLicensePlate() : "-");
            addInfoRow(vehicleTable, "Kilométrage départ", rental.mileageBefore() != null ? rental.mileageBefore().toPlainString() + " km" : "-");
            document.add(vehicleTable);
            document.add(Chunk.NEWLINE);

            // Informations client
            document.add(sectionTitle("LOCATAIRE"));
            PdfPTable clientTable = infoTable();
            addInfoRow(clientTable, "Nom complet", rental.clientFullName() != null ? rental.clientFullName() : "-");
            document.add(clientTable);
            document.add(Chunk.NEWLINE);

            // Informations location
            document.add(sectionTitle("DÉTAILS DE LA LOCATION"));
            PdfPTable rentalTable = infoTable();
            addInfoRow(rentalTable, "Date de début", rental.startDate() != null ? rental.startDate().format(DATE_FMT) : "-");
            addInfoRow(rentalTable, "Date de fin prévue", rental.endDate() != null ? rental.endDate().format(DATE_FMT) : "-");
            if (rental.actualReturnDate() != null) {
                addInfoRow(rentalTable, "Date de retour réelle", rental.actualReturnDate().format(DATE_FMT));
            }
            addInfoRow(rentalTable, "Assurance", rental.insuranceName() != null ? rental.insuranceName() : "Sans assurance");
            addInfoRow(rentalTable, "Caution", rental.deposit() != null ? rental.deposit().toPlainString() + " MAD" : "0 MAD");
            addInfoRow(rentalTable, "Montant total", rental.totalAmount() != null ? rental.totalAmount().toPlainString() + " MAD" : "-");
            addInfoRow(rentalTable, "Statut", rental.status() != null ? rental.status().name() : "-");
            document.add(rentalTable);
            document.add(Chunk.NEWLINE);

            addSeparator(document);

            // Conditions générales
            document.add(sectionTitle("CONDITIONS GÉNÉRALES"));
            String conditions = "Le locataire s'engage à restituer le véhicule dans l'état dans lequel il l'a reçu, "
                    + "à la date et au lieu convenus. Tout dommage constaté au retour sera facturé au locataire. "
                    + "La caution sera restituée après vérification complète du véhicule. "
                    + "Le locataire est responsable des infractions commises pendant la durée de la location.";
            Paragraph cond = new Paragraph(conditions, VALUE_FONT);
            cond.setSpacingAfter(30);
            document.add(cond);

            // Zone de signatures
            document.add(sectionTitle("SIGNATURES"));
            PdfPTable sigTable = new PdfPTable(2);
            sigTable.setWidthPercentage(100);
            sigTable.setSpacingAfter(10);

            PdfPCell agentCell = new PdfPCell();
            agentCell.setBorder(Rectangle.NO_BORDER);
            agentCell.addElement(new Paragraph("Pour l'agence :", LABEL_FONT));
            agentCell.addElement(new Paragraph("\n\n\n_________________________", VALUE_FONT));
            agentCell.addElement(new Paragraph("Signature et cachet", LABEL_FONT));
            agentCell.setPadding(10);

            PdfPCell clientCell = new PdfPCell();
            clientCell.setBorder(Rectangle.NO_BORDER);
            clientCell.addElement(new Paragraph("Le locataire :", LABEL_FONT));
            clientCell.addElement(new Paragraph("(Lu et approuvé)", LABEL_FONT));
            clientCell.addElement(new Paragraph("\n\n_________________________", VALUE_FONT));
            clientCell.addElement(new Paragraph("Signature", LABEL_FONT));
            clientCell.setPadding(10);

            sigTable.addCell(agentCell);
            sigTable.addCell(clientCell);
            document.add(sigTable);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Erreur génération contrat PDF", e);
            throw new RuntimeException("Erreur de génération du contrat PDF", e);
        }
    }

    private Paragraph sectionTitle(String text) {
        Paragraph p = new Paragraph(text, SECTION_FONT);
        p.setSpacingBefore(6);
        p.setSpacingAfter(6);
        return p;
    }

    private void addSeparator(Document document) throws DocumentException {
        Paragraph sep = new Paragraph(" ");
        sep.setSpacingAfter(2);
        document.add(sep);
    }

    private PdfPTable infoTable() throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{2f, 4f});
        table.setWidthPercentage(100);
        return table;
    }

    private void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorderColor(new Color(220, 220, 220));
        labelCell.setPadding(6);
        labelCell.setBackgroundColor(new Color(248, 249, 250));

        PdfPCell valueCell = new PdfPCell(new Phrase(value, VALUE_FONT));
        valueCell.setBorderColor(new Color(220, 220, 220));
        valueCell.setPadding(6);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}

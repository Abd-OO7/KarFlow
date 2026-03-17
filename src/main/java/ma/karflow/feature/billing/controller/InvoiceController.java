package ma.karflow.feature.billing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.billing.dto.InvoiceResponse;
import ma.karflow.feature.billing.dto.PaymentRequest;
import ma.karflow.feature.billing.dto.PaymentResponse;
import ma.karflow.feature.billing.enums.InvoiceStatus;
import ma.karflow.feature.billing.service.InvoiceService;
import ma.karflow.shared.dto.ApiResponse;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoice", description = "Gestion des factures et paiements")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @Operation(summary = "Lister les factures")
    @PreAuthorize("hasAuthority('INVOICE_MANAGE')")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceResponse>>> getAll(
            @RequestParam(required = false) InvoiceStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<InvoiceResponse> result = status != null
                ? invoiceService.getByStatus(status, pageable)
                : invoiceService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une facture avec ses lignes")
    @PreAuthorize("hasAuthority('INVOICE_MANAGE')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getById(id)));
    }

    @PostMapping("/generate/{rentalId}")
    @Operation(summary = "Générer une facture pour une location")
    @PreAuthorize("hasAuthority('INVOICE_MANAGE')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generate(@PathVariable UUID rentalId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(invoiceService.generateForRental(rentalId), "Facture générée"));
    }

    @PostMapping("/{id}/payments")
    @Operation(summary = "Enregistrer un paiement")
    @PreAuthorize("hasAuthority('INVOICE_MANAGE')")
    public ResponseEntity<ApiResponse<PaymentResponse>> addPayment(@PathVariable UUID id,
                                                                     @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(invoiceService.addPayment(id, request), "Paiement enregistré"));
    }

    @GetMapping("/{id}/payments")
    @Operation(summary = "Lister les paiements d'une facture")
    @PreAuthorize("hasAuthority('INVOICE_MANAGE')")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getPayments(
            @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getPayments(id, pageable)));
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Télécharger la facture en PDF")
    @PreAuthorize("hasAuthority('INVOICE_MANAGE')")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID id) {
        byte[] pdf = invoiceService.generatePdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=facture-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/{id}/send-email")
    @Operation(summary = "Envoyer la facture par email au client")
    @PreAuthorize("hasAuthority('INVOICE_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> sendByEmail(@PathVariable UUID id) {
        invoiceService.sendByEmail(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Facture envoyée par email"));
    }
}

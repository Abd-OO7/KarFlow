package ma.karflow.feature.billing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.feature.billing.enums.InvoiceStatus;
import ma.karflow.feature.rental.entity.Rental;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoice")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
public class Invoice extends BaseEntity {

    @Column(name = "invoice_number", nullable = false, length = 30)
    private String invoiceNumber;

    @Column(name = "subtotal", nullable = false)
    private double subtotal;

    @Column(name = "tax_rate", nullable = false)
    private double taxRate = 20.0;

    @Column(name = "tax_amount", nullable = false)
    private double taxAmount;

    @Column(name = "discount")
    private double discount;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceLine> lines = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<Payment> payments = new ArrayList<>();

    public void recalculate() {
        this.subtotal = lines.stream().mapToDouble(InvoiceLine::getTotalPrice).sum();
        this.taxAmount = subtotal * (taxRate / 100.0);
        this.totalAmount = subtotal + taxAmount - discount;
    }

    public double getTotalPaid() {
        return payments.stream().mapToDouble(Payment::getAmount).sum();
    }
}

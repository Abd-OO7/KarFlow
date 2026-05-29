package ma.karflow.feature.billing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.feature.billing.enums.InvoiceStatus;
import ma.karflow.feature.rental.entity.Rental;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate = new BigDecimal("20.00");

    @Column(name = "tax_amount", nullable = false)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount")
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

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
        this.subtotal = lines.stream()
                .map(InvoiceLine::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.taxAmount = subtotal.multiply(taxRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        this.totalAmount = subtotal.add(taxAmount).subtract(discount != null ? discount : BigDecimal.ZERO);
    }

    public BigDecimal getTotalPaid() {
        return payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

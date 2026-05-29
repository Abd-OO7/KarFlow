package ma.karflow.feature.billing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.feature.billing.enums.InvoiceLineType;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "invoice_line")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
public class InvoiceLine extends BaseEntity {

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_type", nullable = false, length = 30)
    private InvoiceLineType lineType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    public InvoiceLine(String label, long quantity, BigDecimal unitPrice, InvoiceLineType lineType) {
        this.label = label;
        this.quantity = BigDecimal.valueOf(quantity);
        this.unitPrice = unitPrice;
        this.totalPrice = this.quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        this.lineType = lineType;
    }

    public InvoiceLine(String label, BigDecimal quantity, BigDecimal unitPrice, InvoiceLineType lineType) {
        this.label = label;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        this.lineType = lineType;
    }
}

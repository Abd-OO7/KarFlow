package ma.karflow.feature.billing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.feature.billing.enums.InvoiceLineType;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

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
    private double quantity = 1;

    @Column(name = "unit_price", nullable = false)
    private double unitPrice;

    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_type", nullable = false, length = 30)
    private InvoiceLineType lineType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    public InvoiceLine(String label, double quantity, double unitPrice, InvoiceLineType lineType) {
        this.label = label;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = quantity * unitPrice;
        this.lineType = lineType;
    }
}

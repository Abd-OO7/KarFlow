package ma.karflow.feature.billing.service;

import ma.karflow.feature.billing.dto.InvoiceResponse;
import ma.karflow.feature.billing.dto.PaymentRequest;
import ma.karflow.feature.billing.dto.PaymentResponse;
import ma.karflow.feature.billing.entity.Invoice;
import ma.karflow.feature.billing.entity.InvoiceLine;
import ma.karflow.feature.billing.entity.Payment;
import ma.karflow.feature.billing.enums.InvoiceLineType;
import ma.karflow.feature.billing.enums.InvoiceStatus;
import ma.karflow.feature.billing.enums.PaymentMethod;
import ma.karflow.feature.billing.mapper.InvoiceMapper;
import ma.karflow.feature.billing.mapper.PaymentMapper;
import ma.karflow.feature.billing.repository.InvoiceRepository;
import ma.karflow.feature.billing.repository.PaymentRepository;
import ma.karflow.feature.client.entity.Client;
import ma.karflow.feature.rental.entity.Rental;
import ma.karflow.feature.rental.enums.RentalStatus;
import ma.karflow.feature.rental.repository.RentalRepository;
import ma.karflow.feature.vehicle.entity.Brand;
import ma.karflow.feature.vehicle.entity.Category;
import ma.karflow.feature.vehicle.entity.Vehicle;
import ma.karflow.feature.vehicle.entity.VehicleModel;
import ma.karflow.feature.vehicle.enums.VehicleStatus;
import ma.karflow.shared.exception.BusinessException;
import ma.karflow.shared.util.EmailService;
import ma.karflow.shared.util.PdfGenerator;
import ma.karflow.shared.util.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private RentalRepository rentalRepository;
    @Mock private InvoiceMapper invoiceMapper;
    @Mock private PaymentMapper paymentMapper;
    @Mock private PdfGenerator pdfGenerator;
    @Mock private EmailService emailService;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private final UUID tenantId = UUID.randomUUID();
    private Rental rental;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        Category category = new Category();
        category.setDailyRateMultiplier(1.0);

        Brand brand = new Brand();
        brand.setName("Toyota");

        VehicleModel model = new VehicleModel();
        model.setName("Corolla");
        model.setBrand(brand);

        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate("AB-123-CD");
        vehicle.setDailyRate(300.0);
        vehicle.setStatus(VehicleStatus.RENTED);
        vehicle.setCategory(category);
        vehicle.setVehicleModel(model);

        Client client = new Client();
        client.setFirstName("Ahmed");
        client.setLastName("Benali");
        client.setEmail("ahmed@test.ma");

        rental = new Rental();
        rental.setId(UUID.randomUUID());
        rental.setTenantId(tenantId);
        rental.setStartDate(LocalDate.now().minusDays(5));
        rental.setEndDate(LocalDate.now());
        rental.setStatus(RentalStatus.RETURNED);
        rental.setMileageBefore(50000.0);
        rental.setMileageAfter(50500.0);
        rental.setVehicle(vehicle);
        rental.setClient(client);

        InvoiceLine line = new InvoiceLine("Location 5 jours", 5, 300.0, InvoiceLineType.RENTAL_DAYS);
        invoice = new Invoice();
        invoice.setId(UUID.randomUUID());
        invoice.setTenantId(tenantId);
        invoice.setInvoiceNumber("INV-2026-00001");
        invoice.setRental(rental);
        invoice.setSubtotal(1500.0);
        invoice.setTaxRate(20.0);
        invoice.setTaxAmount(300.0);
        invoice.setTotalAmount(1800.0);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setLines(new ArrayList<>(List.of(line)));
        invoice.setPayments(new ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void generateForRental_shouldCreateInvoiceWithLines() {
        when(invoiceRepository.findByRentalIdAndTenantId(rental.getId(), tenantId)).thenReturn(Optional.empty());
        when(rentalRepository.findByIdAndTenantId(rental.getId(), tenantId)).thenReturn(Optional.of(rental));
        when(invoiceRepository.findMaxSequenceByPrefix(eq(tenantId), anyString())).thenReturn(0);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceResponse expected = new InvoiceResponse(UUID.randomUUID(), "INV-2026-00001",
                1500, 20, 300, 0, 1800, 0, 1800, InvoiceStatus.DRAFT,
                null, null, rental.getId(), "Ahmed Benali", "AB-123-CD", List.of(), null);
        when(invoiceMapper.toResponse(any(Invoice.class))).thenReturn(expected);

        InvoiceResponse result = invoiceService.generateForRental(rental.getId());

        assertNotNull(result);
        assertEquals("INV-2026-00001", result.invoiceNumber());
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void addPayment_partialPayment_shouldNotMarkAsPaid() {
        PaymentRequest request = new PaymentRequest(500, PaymentMethod.CASH, null, null);
        PaymentResponse expected = new PaymentResponse(UUID.randomUUID(), 500, LocalDateTime.now(),
                PaymentMethod.CASH, null, null, invoice.getId(), null);

        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(expected);

        PaymentResponse result = invoiceService.addPayment(invoice.getId(), request);

        assertEquals(500, result.amount());
        assertEquals(InvoiceStatus.SENT, invoice.getStatus()); // DRAFT → SENT after partial payment
        assertNull(invoice.getPaidDate());
    }

    @Test
    void addPayment_fullPayment_shouldMarkAsPaid() {
        PaymentRequest request = new PaymentRequest(1800, PaymentMethod.CREDIT_CARD, "TXN-123", null);
        PaymentResponse expected = new PaymentResponse(UUID.randomUUID(), 1800, LocalDateTime.now(),
                PaymentMethod.CREDIT_CARD, "TXN-123", null, invoice.getId(), null);

        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(expected);

        invoiceService.addPayment(invoice.getId(), request);

        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertNotNull(invoice.getPaidDate());
    }

    @Test
    void addPayment_toAlreadyPaidInvoice_shouldThrow() {
        invoice.setStatus(InvoiceStatus.PAID);
        PaymentRequest request = new PaymentRequest(100, PaymentMethod.CASH, null, null);

        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId)).thenReturn(Optional.of(invoice));

        assertThrows(BusinessException.class, () -> invoiceService.addPayment(invoice.getId(), request));
    }

    @Test
    void addPayment_exceedingAmount_shouldThrow() {
        PaymentRequest request = new PaymentRequest(5000, PaymentMethod.CASH, null, null);

        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId)).thenReturn(Optional.of(invoice));

        assertThrows(BusinessException.class, () -> invoiceService.addPayment(invoice.getId(), request));
    }
}

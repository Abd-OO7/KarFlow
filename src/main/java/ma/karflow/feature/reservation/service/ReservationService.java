package ma.karflow.feature.reservation.service;

import ma.karflow.feature.rental.dto.RentalResponse;
import ma.karflow.feature.reservation.dto.ReservationCreateRequest;
import ma.karflow.feature.reservation.dto.ReservationResponse;
import ma.karflow.feature.reservation.enums.ReservationStatus;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReservationService {

    ReservationResponse createFromBooking(ReservationCreateRequest request);

    ReservationResponse confirm(UUID reservationId);

    ReservationResponse cancel(UUID reservationId, String reason);

    RentalResponse convertToRental(UUID reservationId);

    PageResponse<ReservationResponse> getAll(Pageable pageable);

    PageResponse<ReservationResponse> getByStatus(ReservationStatus status, Pageable pageable);

    ReservationResponse getById(UUID id);

    PageResponse<ReservationResponse> getByClientEmail(String email, Pageable pageable);
}

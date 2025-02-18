package roomescape.web.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.CancelService;
import roomescape.application.ReservationService;
import roomescape.application.dto.request.member.MemberInfo;
import roomescape.application.dto.request.reservation.ReservationPaymentRequest;
import roomescape.application.dto.request.reservation.UserReservationRequest;
import roomescape.application.dto.response.reservation.ReservationResponse;
import roomescape.application.dto.response.reservation.UserReservationResponse;

@RestController
@RequiredArgsConstructor
public class MemberReservationController {
    private final ReservationService reservationService;
    private final CancelService cancelService;

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> makeReservation(
            @RequestBody @Valid UserReservationRequest request,
            MemberInfo memberInfo
    ) {
        ReservationResponse response = reservationService.saveReservation(request, memberInfo.id());
        return ResponseEntity.created(URI.create("/reservations/" + response.id()))
                .body(response);
    }

    @PostMapping("/reservations/payment")
    public ResponseEntity<ReservationResponse> paymentForPending(
            @RequestBody ReservationPaymentRequest request,
            MemberInfo memberInfo
    ) {
        ReservationResponse response = reservationService.paymentForPending(request, memberInfo.id());
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<UserReservationResponse>> findAllMyReservations(MemberInfo memberInfo) {
        List<UserReservationResponse> response = reservationService.findAllWithRank(memberInfo.id());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/waitings/{idWaiting}")
    public ResponseEntity<Void> cancelWaiting(
            @PathVariable(value = "idWaiting") Long waitingId,
            MemberInfo memberInfo
    ) {
        cancelService.cancelReservation(waitingId, memberInfo);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reservations/{idReservation}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable(value = "idReservation") Long reservationId,
            MemberInfo memberInfo
    ) {
        cancelService.cancelReservation(reservationId, memberInfo);
        return ResponseEntity.noContent().build();
    }
}

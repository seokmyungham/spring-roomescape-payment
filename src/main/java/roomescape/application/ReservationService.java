package roomescape.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.request.payment.PaymentRequest;
import roomescape.application.dto.request.reservation.ReservationPaymentRequest;
import roomescape.application.dto.request.reservation.ReservationRequest;
import roomescape.application.dto.request.reservation.ReservationSearchCondition;
import roomescape.application.dto.request.reservation.UserReservationRequest;
import roomescape.application.dto.response.payment.PaymentResponse;
import roomescape.application.dto.response.reservation.ReservationResponse;
import roomescape.application.dto.response.reservation.UserReservationResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.payment.Payment;
import roomescape.domain.payment.PaymentClient;
import roomescape.domain.payment.PaymentRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationFactory;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationWithRank;
import roomescape.domain.reservation.Status;
import roomescape.exception.member.AuthenticationFailureException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final PaymentClient paymentRestClient;
    private final PaymentRepository paymentRepository;

    private final ReservationFactory reservationFactory;

    @Transactional
    public ReservationResponse saveReservation(UserReservationRequest request, Long memberId) {
        Member member = memberRepository.findMember(memberId).orElseThrow(AuthenticationFailureException::new);
        Reservation reservation = reservationFactory.create(
                request.themeId(), request.date(), request.timeId(), member);
        reservationRepository.save(reservation);

        if (reservation.isReserved()) {
            PaymentRequest paymentRequest = request.toPaymentRequest();
            PaymentResponse paymentResponse = paymentRestClient.confirm(paymentRequest);
            Payment payment = paymentRepository.save(paymentResponse.toPayment());
            reservation.setPayment(payment);
        }
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse saveReservationByAdmin(ReservationRequest request) {
        Member member = memberRepository.findMember(request.memberId())
                .orElseThrow(AuthenticationFailureException::new);
        Reservation reservation = reservationFactory.create(
                request.themeId(), request.date(), request.timeId(), member);
        reservationRepository.save(reservation);
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse paymentForPending(ReservationPaymentRequest request, Long memberId) {
        Reservation reservation = reservationRepository.getReservation(request.reservationId());
        reservation.validateOwner(memberId);

        PaymentRequest paymentRequest = request.toPaymentRequest();
        PaymentResponse paymentResponse = paymentRestClient.confirm(paymentRequest);
        Payment payment = paymentRepository.save(paymentResponse.toPayment());

        reservation.setPayment(payment);
        reservation.toReserved();
        return ReservationResponse.from(reservation);
    }

    public List<ReservationResponse> findAllReservationsWithReserved() {
        List<Reservation> reservations = reservationRepository.findAll(Status.RESERVED);
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllReservationByConditions(ReservationSearchCondition condition) {
        List<Reservation> reservations = reservationRepository.findReservation(
                condition.start(), condition.end(), condition.memberId(), condition.themeId());
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<UserReservationResponse> findAllWithRank(Long memberId) {
        List<ReservationWithRank> reservationWithRanks = reservationRepository.findWithRank(memberId);

        return reservationWithRanks.stream()
                .map(UserReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllWaitings() {
        List<Reservation> reservations = reservationRepository.findAll(Status.WAITING);
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }
}

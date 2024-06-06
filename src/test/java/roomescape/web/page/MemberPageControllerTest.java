package roomescape.web.page;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MemberPageControllerTest {
    
    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("유저 자신의 예약 페이지 호출 테스트")
    @Test
    void user_my_reservation_page() {
        RestAssured.given().log().all()
                .when().get("/reservation-mine")
                .then().log().all()
                .statusCode(200);
    }
}

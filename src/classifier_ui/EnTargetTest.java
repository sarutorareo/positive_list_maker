package classifier_ui;

import static org.junit.jupiter.api.Assertions.*;

public class EnTargetTest {
    @org.junit.jupiter.api.Test
    void test_toInt() {
        EnTarget t = EnTarget.Player;
        assertEquals(0, t.toInt());

        t = EnTarget.DealerButton;
        assertEquals(1, t.toInt());
    }

    @org.junit.jupiter.api.Test
    void test_formInt() {
        assertEquals(EnTarget.Player, EnTarget.fromInt(0));
        assertEquals(EnTarget.DealerButton, EnTarget.fromInt(1));
    }
}

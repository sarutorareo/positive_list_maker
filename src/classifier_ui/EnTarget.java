package classifier_ui;

public enum EnTarget {
    Player,
    DealerButton;
    public int toInt() {
        switch (this) {
            case Player:
                return 0;
            case DealerButton:
                return 1;
            default:
                assert(false);
                return -1;
        }
    }
    static public EnTarget fromInt(int i) {
        switch (i) {
            case 0:
                return Player;
            case 1:
                return DealerButton;
            default:
                assert(false);
                return null;
        }
    }
}

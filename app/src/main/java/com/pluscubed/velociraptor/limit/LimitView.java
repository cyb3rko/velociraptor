package com.pluscubed.velociraptor.limit;

public interface LimitView {
    void stop();

    void changeConfig();

    void setSpeed(int speed, int speedLimitWarning);

    void setSpeeding(boolean speeding);

    void setDebuggingText(String text);

    void setLimit(String limit, String source);

    void updatePrefs();

    void hideLimit(boolean hideLimit);
}

package common;

import java.awt.event.ActionEvent;

public interface ActionTarget {
    void dispatch(ActionType actionType);
}

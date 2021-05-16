
public class StateMachine {

    private States currentState;
    private States previousState;

    public StateMachine() {
        this.currentState = States.START;
        this.previousState = currentState;
    }

    public void setCurrentState(States newState) {
        previousState = currentState;
        currentState = newState;
    }

    public void setPreviousState(States previousState) {
        this.previousState = previousState;
    }

    public States getCurrentState() {
        return currentState;
    }

    public States getPreviousState() {
        return previousState;
    }
}


public class StateMachine {

    public States currentState;

    public StateMachine() {
        this.currentState = States.START;
    }

    public void setCurrentState(States newState) {
        currentState = newState;
    }

    public States getCurrentState() {
        return currentState;
    }

}

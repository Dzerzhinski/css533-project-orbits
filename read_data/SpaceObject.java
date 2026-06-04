

public class SpaceObject {

    int id;
    String name;
    double[] state;

    SpaceObject() {
        id = 0;
        name = "";
    }

    SpaceObject(int idTarget, String nameTarget, double[] vector) {
        this.id = idTarget;
        this.name = nameTarget;
        state = new double[6];
        for(int i = 0; i < vector.length; i++) {
            state[i] = vector[i];
        }
    }

    public int getTargetId() { return this.id; }
    public String getTargetName() { return this.name; }

    public double[] getStateVector() {
        double[] v = new double[6];
        for(int i = 0; i < 6; i++) {
            v[i] = state[i];
        }
        return v;
    }

}



import java.util.ArrayList;
import java.util.List;

public class Sector {
    public int floorHeight;
    public int ceilingHeight;
    public List<Linedef> linedefs;

    public Sector() {
        linedefs = new ArrayList<>();
    }

    public void arrangeLinedefs() {
        List<Linedef> linedefs2 = new ArrayList<>();
        linedefs2.add(linedefs.get(0));
        // The last you were going with this, you had just added a list to each linedef storing each linedef it's connected to at the start and end.
        // You were going to traverse this sequence of linedefs to get the linedefs that are directly connected to each other.
        Linedef lastDefAdded = linedefs.get(0);
        for (int i = 0; i < linedefs.size(); i++) {
            for (Linedef l : linedefs) {
                if (lastDefAdded.connectedLinedefsEnd.contains(l)) {
                    linedefs2.add(l);
                    lastDefAdded = l;
                }
            }
        }
        linedefs = linedefs2;

    }
}

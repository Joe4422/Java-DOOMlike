// A vertex is a single point on the 2D world. Three vertices make up a triangle, and one linedef is connected between two vertices.

public class Vertex {
    public int x;
    public int y;

    public Vertex(short x, short y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    public Vertex(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Vertex v) {
        return (this.x == v.x && this.y == v.y);
    }
}

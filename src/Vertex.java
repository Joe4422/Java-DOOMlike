// A vertex is a single point on the 2D world. Three vertices make up a triangle, and one linedef is connected between two vertices.

public class Vertex {
    public double x;
    public double y;
    public Linedef[] linedefs;
    public Triangle[] triangles;

    public Vertex(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void rotate(int degrees, Vertex origin) {
        double s = Math.sin(Math.toRadians(degrees));
        double c = Math.cos(Math.toRadians(degrees));
        x -= origin.x;
        y -= origin.y;
        x = x * c - y * s;
        y = x * s + y * c;
        x += origin.x;
        y += origin.y;
    }

    public static Vertex rotate(int degrees, Vertex origin, Vertex point) {
        double s = Math.sin(Math.toRadians(degrees));
        double c = Math.cos(Math.toRadians(degrees));
        point.x -= origin.x;
        point.y -= origin.y;
        point.x = point.x * c - point.y * s;
        point.y = point.x * s + point.y * c;
        point.x += origin.x;
        point.y += origin.y;
        return point;
    }
}

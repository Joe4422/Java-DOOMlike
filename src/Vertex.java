/* A vertex is a single point on the 2D world. Three vertices make up a triangle, and one linedef is connected between two vertices.*/

public class Vertex {
    public float x;
    public float y;
    public Linedef[] linedefs;
    public Triangle[] triangles;
}

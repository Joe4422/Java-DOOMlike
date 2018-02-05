/* A linedef is basically a wall, or an edge. It is owned by two vertices. */

import java.util.List;

public class Linedef {

    public Vertex startVertex;
    public Vertex endVertex;
    public int specialType;
    public int sectorTag;
    public Sidedef rightSidedef;
    public Sidedef leftSidedef;
    public boolean mark = false;
    public List<Linedef> connectedLinedefsStart;
    public List<Linedef> connectedLinedefsEnd;


    // Flags
    public boolean blocksPlayersAndMonsters;
    public boolean blocksMonsters;
    public boolean twoSided;
    public boolean upperTextureUnpegged;
    public boolean lowerTextureUnpegged;
    public boolean secret;
    public boolean blocksSound;
    public boolean neverShowOnAutomap;
    public boolean alwaysShowOnAutomap;
}

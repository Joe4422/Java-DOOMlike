import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class DoomMapReader {

    private Path wadLocation;
    private byte[] wadBytes;
    private int numLumps;
    private int tableLocation;
    public Linedef[] linedefs;
    public Sector[] sectors;

    DoomMapReader(String path, String map) {
        wadLocation = Paths.get(path);
        try {
            wadBytes = getByteArray();
        } catch (IOException e) {
            System.err.println("Wad file not found!");
            System.exit(1);
        }
        readLumpCount();
        getLumpDirectory();
        sectors = readSectors(map);
        System.out.println(sectors.length);
        linedefs = readLinedefs(map);
        for (Sector s : sectors) {
            for (Linedef l : linedefs) {
                try {
                    if ((l.leftSidedef.sector == s)) {
                        s.linedefs.add(l);
                    }
                } catch (NullPointerException e) {}
                try {
                    if ((l.rightSidedef.sector == s && !s.linedefs.contains(l))) {
                        s.linedefs.add(l);
                    }
                } catch (NullPointerException e) {}
            }
            s.arrangeLinedefs();
        }
    }

    private Sidedef[] readSidedefs(String mapName) {
        int sidedefsLumpStart = 0;
        int lumpDataPointer = 0;
        int lumpSizeBytes = 0;
        for (int i = 0; i < numLumps; i++) {
            byte[] currentBytes = readBytesFromFile(8, tableLocation + (i * 16) + 8 );
            if (cbytes2string(currentBytes).startsWith(mapName)) {
                sidedefsLumpStart = tableLocation + ((i + 3) * 16);
                lumpDataPointer = c4bytes2intLE(readBytesFromFile(4, sidedefsLumpStart));
                lumpSizeBytes = c4bytes2intLE(readBytesFromFile(4, sidedefsLumpStart + 4));
            }
        }
        Sidedef[] sidedefs = new Sidedef[lumpSizeBytes / 30];
        for (int i = 0; i < lumpSizeBytes / 30; i++) {
            Sidedef s = new Sidedef();
            s.sector = sectors[c2bytes2shortLE(readBytesFromFile(2, lumpDataPointer + (i * 30) + 28))];
            sidedefs[i] = s;
        }
        System.out.println(sidedefs.length);
        return sidedefs;
    }

    private Sector[] readSectors(String mapName) {
        int sectorsLumpStart = 0;
        int lumpDataPointer = 0;
        int lumpSizeBytes = 0;
        for (int i = 0; i < numLumps; i++) {
            byte[] currentBytes = readBytesFromFile(8, tableLocation + (i * 16) + 8 );
            if (cbytes2string(currentBytes).startsWith(mapName)) {
                sectorsLumpStart = tableLocation + ((i + 8) * 16);
                lumpDataPointer = c4bytes2intLE(readBytesFromFile(4, sectorsLumpStart));
                lumpSizeBytes = c4bytes2intLE(readBytesFromFile(4, sectorsLumpStart + 4));
            }
        }
        Sector[] sectors = new Sector[lumpSizeBytes / 26];
        for (int i = 0; i < lumpSizeBytes / 26; i++) {
            Sector s = new Sector();
            s.floorHeight = (int) c2bytes2shortLE(readBytesFromFile(2, lumpDataPointer + (i * 26)));
            s.ceilingHeight = (int) c2bytes2shortLE(readBytesFromFile(2, lumpDataPointer + (i * 26) + 2));
            sectors[i] = s;
        }
        return sectors;
    }

    private Linedef[] readLinedefs(String mapName) {
        int lineDefsLumpStart = 0;
        int lumpDataPointer = 0;
        int lumpSizeBytes = 0;
        for (int i = 0; i < numLumps; i++) {
            byte[] currentBytes = readBytesFromFile(8, tableLocation + (i * 16) + 8 );
            if (cbytes2string(currentBytes).startsWith(mapName)) {
                lineDefsLumpStart = tableLocation + ((i + 2) * 16);
                lumpDataPointer = c4bytes2intLE(readBytesFromFile(4, lineDefsLumpStart));
                lumpSizeBytes = c4bytes2intLE(readBytesFromFile(4, lineDefsLumpStart + 4));
            }
        }
        Linedef[] linedefs = new Linedef[lumpSizeBytes / 14];
        Vertex[] vertices = readVertices(mapName);
        Sidedef[] sidedefs = readSidedefs(mapName);
        for (int i = 0; i < lumpSizeBytes / 14; i++) {
            Linedef linedef = new Linedef();
            linedef.startVertex = vertices[c2bytes2shortLE(readBytesFromFile(2, lumpDataPointer + (i * 14)))];
            try {
                for (int j = 0; j < i; j++) {
                    if (linedefs[j].startVertex.equals(linedef.startVertex) || linedefs[j].endVertex.equals(linedef.startVertex))
                        linedef.connectedLinedefsStart.add(linedefs[j]);
                }
                linedef.endVertex = vertices[c2bytes2shortLE(readBytesFromFile(2, lumpDataPointer + (i * 14) + 2))];
                for (int j = 0; j < i; j++) {
                    if (linedefs[j].endVertex.equals(linedef.endVertex) || linedefs[j].endVertex.equals(linedef.endVertex))
                        linedef.connectedLinedefsStart.add(linedefs[j]);
                }
            }catch (NullPointerException e) {

                }
            try {
                linedef.rightSidedef = sidedefs[c2bytes2shortLE(readBytesFromFile(2, lumpDataPointer + (i * 14) + 10))];
            } catch (ArrayIndexOutOfBoundsException e) {
                linedef.rightSidedef = null;
            }
            try {
                linedef.leftSidedef = sidedefs[c2bytes2shortLE(readBytesFromFile(2, lumpDataPointer + (i * 14) + 12))];
            } catch (ArrayIndexOutOfBoundsException e) {
                linedef.leftSidedef = null;
            }
            short flagBytes = c2bytes2shortLE(readBytesFromFile(2, lumpDataPointer + (i * 14) + 4));
            String flagString = Integer.toBinaryString(flagBytes);
            for (int j = 0; j < flagString.length(); j++) {
                if (j == 0) {
                    linedef.blocksPlayersAndMonsters = flagString.charAt(j) == '1';
                }
                if (j == 1) {
                    linedef.blocksMonsters = flagString.charAt(j) == '1';
                }
                if (j == 2) {
                    linedef.twoSided = flagString.charAt(j) == '1';
                }
                if (j == 3) {
                    linedef.upperTextureUnpegged = flagString.charAt(j) == '1';
                }
                if (j == 4) {
                    linedef.lowerTextureUnpegged = flagString.charAt(j) == '1';
                }
            }
            linedefs[i] = linedef;

        }
        return linedefs;
    }

    private Vertex[] readVertices(String mapName) {
        int verticesLumpStart = 0;
        int lumpDataPointer = 0;
        int lumpSizeBytes = 0;
        for (int i = 0; i < numLumps; i++) {
            byte[] currentBytes = readBytesFromFile(8, tableLocation + (i * 16) + 8 );
            if (cbytes2string(currentBytes).startsWith(mapName)) {
                verticesLumpStart = tableLocation + ((i + 4) * 16);
                lumpDataPointer = c4bytes2intLE(readBytesFromFile(4, verticesLumpStart));
                lumpSizeBytes = c4bytes2intLE(readBytesFromFile(4, verticesLumpStart + 4));
            }
        }
        Vertex[] vertices = new Vertex[lumpSizeBytes / 4];
        for (int i = 0; i < lumpSizeBytes / 4; i++) {
            short x = c2bytes2shortLE(readBytesFromFile(2, lumpDataPointer + (i * 4)));
            short y = c2bytes2shortLE(readBytesFromFile(2, lumpDataPointer + (i * 4) + 2));
            Vertex v = new Vertex(x, y);
            vertices[i] = v;
        }
        return vertices;
    }

    private String[] readLumpNames() {
        String[] lumpNames = new String[numLumps];
        for (int i = 0; i < numLumps; i++) {
            byte[] currentBytes = readBytesFromFile(8, tableLocation + (i * 16) + 8 );
            lumpNames[i] = cbytes2string(currentBytes);
        }
        return lumpNames;
    }

    private byte[] getByteArray() throws IOException {
        return Files.readAllBytes(wadLocation);
    }

    private void getLumpDirectory() {
        byte[] lumpArray = readBytesFromFile(4, 8);
        tableLocation = c4bytes2intLE(lumpArray);
        System.out.println(tableLocation);
    }

    private byte[] readBytesFromFile(int bytesToRead, int offset) {
        byte[] lumpArray = new byte[bytesToRead];
        for (int i = 0; i < bytesToRead; i++) {
            lumpArray[i] = wadBytes[offset + i];
        }
        return lumpArray;
    }

    private void readLumpCount() {
        byte[] lumpArray = readBytesFromFile(4, 4);
        numLumps = c4bytes2intLE(lumpArray);
        System.out.println(numLumps);
    }

    public static int c4bytes2intLE(byte[] bytes) {
        int newInt;
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (byte b : bytes) {
            buffer.put(b);
        }
        buffer.flip();
        newInt = buffer.getInt();
        return newInt;
    }

    public static short c2bytes2shortLE(byte[] bytes) {
        short newShort;
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (byte b : bytes) {
            buffer.put(b);
        }
        buffer.flip();
        newShort = buffer.getShort();
        return newShort;
    }

    public static String cbytes2string(byte[] bytes) {
        String outStr = new String(bytes, StandardCharsets.US_ASCII);
        return outStr;
    }

}

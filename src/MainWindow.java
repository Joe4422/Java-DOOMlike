import javafx.geometry.Point3D;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class MainWindow {
    private long window;
    DoomMapReader wad;
    Boolean alternate = false;
    Boolean drawFloors = true;
    float aspect;
    int fov = 30;
    int segOrder = 0;
    List<Linedef> renderList;

    public void run() {
        renderList = new ArrayList<>();
        wad = new DoomMapReader("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Ultimate Doom\\base\\DOOM.WAD", "E1M1");

        init();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) throw new IllegalStateException("Unable to initialise GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(640, 480, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        try (MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);

        glfwSwapInterval(1);

        glfwShowWindow(window);
    }

    /* for resizing window */
    private GLFWFramebufferSizeCallback resizeWindow = new GLFWFramebufferSizeCallback(){
        @Override
        public void invoke(long window, int width, int height){
            glViewport(0,0,width,height);
            aspect = (float) width / (float) height;
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            perspectiveGL(fov, aspect, 0.1, 10000);
            glMatrixMode(GL_MODELVIEW);
            System.out.println(aspect);
            //update any other window vars you might have (aspect ratio, MVP matrices, etc)
        }
    };

    private void loop() {
        int keyHolder = GLFW_KEY_Q;
        GL.createCapabilities();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        IntBuffer pWidth = BufferUtils.createIntBuffer(1);
        IntBuffer pHeight = BufferUtils.createIntBuffer(1);

        glfwGetWindowSize(window, pWidth, pHeight);

        int width = pWidth.get();
        int height = pHeight.get();

        glViewport(0, 0, width, height);
        aspect = (float) width / (float) height;
        perspectiveGL(fov, aspect, 0.1, 10000);
        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_DEPTH_TEST);
        int i = 0;
        int cameraPitch = 0;
        int cameraYaw = 0;
        Point3D cameraPos = new Point3D(0, -32, 2500);

        GLFW.glfwSetFramebufferSizeCallback(window, resizeWindow);

        DoubleBuffer lastCursorX = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer lastCursorY = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer currentCursorX = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer currentCursorY = BufferUtils.createDoubleBuffer(1);
        while (!glfwWindowShouldClose(window)) {
            int z = -200;
            glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glPushMatrix();

            glRotatef(cameraPitch, 1, 0, 0);
            glRotatef(cameraYaw, 0, 1, 0);
            glTranslatef((float) cameraPos.getX(), (float) cameraPos.getY(), (float) cameraPos.getZ());
            renderList.clear();
            for (Sector s : wad.sectors) {
                alternate = false;
                drawSector(s);
            }

            glPopMatrix();

            i = i >= 360 ? 0 : i + 1;

            glfwSwapBuffers(window);

            glfwPollEvents();

            int sensitivity = 3;
            int speed = 8;

            if ( glfwGetKey(window, GLFW_KEY_UP) == 1) {
                cameraPitch -= sensitivity;
            }
            if ( glfwGetKey(window, GLFW_KEY_DOWN) == 1) {
                cameraPitch += sensitivity;
            }
            if ( glfwGetKey(window, GLFW_KEY_LEFT) == 1) {
                cameraYaw -= sensitivity;
            }
            if ( glfwGetKey(window, GLFW_KEY_RIGHT) == 1) {
                cameraYaw += sensitivity;
            }
            if (glfwGetKey(window, GLFW_KEY_1) == 1) {
                wad = new DoomMapReader("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Ultimate Doom\\base\\DOOM.WAD", "E1M1");
            }
            if (glfwGetKey(window, GLFW_KEY_2) == 1) {
                wad = new DoomMapReader("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Ultimate Doom\\base\\DOOM.WAD", "E1M2");
            }
            if (glfwGetKey(window, GLFW_KEY_3) == 1) {
                wad = new DoomMapReader("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Ultimate Doom\\base\\DOOM.WAD", "E1M3");
            }
            if (glfwGetKey(window, GLFW_KEY_4) == 1) {
                wad = new DoomMapReader("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Ultimate Doom\\base\\DOOM.WAD", "E1M4");
            }
            if (glfwGetKey(window, GLFW_KEY_5) == 1) {
                wad = new DoomMapReader("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Ultimate Doom\\base\\DOOM.WAD", "E1M5");
            }
            if (glfwGetKey(window, GLFW_KEY_6) == 1) {
                wad = new DoomMapReader("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Ultimate Doom\\base\\DOOM.WAD", "E1M6");
            }
            if (glfwGetKey(window, GLFW_KEY_7) == 1) {
                wad = new DoomMapReader("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Ultimate Doom\\base\\DOOM.WAD", "E1M7");
            }
            if (glfwGetKey(window, GLFW_KEY_8) == 1) {
                wad = new DoomMapReader("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Ultimate Doom\\base\\DOOM.WAD", "E1M8");
            }
            if (glfwGetKey(window, GLFW_KEY_9) == 1) {
                wad = new DoomMapReader("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Ultimate Doom\\base\\DOOM.WAD", "E1M9");
            }
            if (glfwGetKey(window, GLFW_KEY_U) == 1) {
                fov += 1;
                glMatrixMode(GL_PROJECTION);
                glLoadIdentity();
                perspectiveGL(fov, aspect, 0.1, 10000);
                glMatrixMode(GL_MODELVIEW);
            }
            if (glfwGetKey(window, GLFW_KEY_J) == 1) {
                fov -= 1;
                glMatrixMode(GL_PROJECTION);
                glLoadIdentity();
                perspectiveGL(fov, aspect, 0.1, 10000);
                glMatrixMode(GL_MODELVIEW);
            }
            if (glfwGetKey(window, GLFW_KEY_I) == GLFW_PRESS) {
                keyHolder = GLFW_KEY_I;
            }
            if (glfwGetKey(window, GLFW_KEY_K) == GLFW_PRESS) {
                keyHolder = GLFW_KEY_K;
            }
            if (keyHolder == GLFW_KEY_I && glfwGetKey(window, GLFW_KEY_I) == GLFW_RELEASE) {
                System.out.println("a");
                segOrder += 1;
                keyHolder = GLFW_KEY_Q;
            }
            if (keyHolder == GLFW_KEY_K && glfwGetKey(window, GLFW_KEY_K) == GLFW_RELEASE) {
                System.out.println("b");
                segOrder -= 1;
                keyHolder = GLFW_KEY_Q;
            }

            glfwSetWindowTitle(window, Integer.toString(segOrder));

            if ( glfwGetKey(window, GLFW_KEY_W) == 1) {
                double dx = Math.sin(Math.toRadians(cameraYaw)) * speed;
                double dz = Math.cos(Math.toRadians(cameraYaw)) * speed;
                double dy = Math.sin(Math.toRadians(cameraPitch)) * speed;
                cameraPos = new Point3D(cameraPos.getX() - dx, cameraPos.getY() + dy, cameraPos.getZ() + dz);
            }
            if ( glfwGetKey(window, GLFW_KEY_S) == 1) {
                double dx = Math.sin(Math.toRadians(cameraYaw)) * speed;
                double dz = Math.cos(Math.toRadians(cameraYaw)) * speed;
                double dy = Math.sin(Math.toRadians(cameraPitch)) * speed;
                cameraPos = new Point3D(cameraPos.getX() + dx, cameraPos.getY() - dy, cameraPos.getZ() - dz);
            }
            if ( glfwGetKey(window, GLFW_KEY_A) == 1) {
                double dx = Math.sin(Math.toRadians(cameraYaw + 90)) * speed;
                double dz = Math.cos(Math.toRadians(cameraYaw + 90)) * speed;
                cameraPos = new Point3D(cameraPos.getX() + dx, cameraPos.getY(), cameraPos.getZ() - dz);
            }
            if ( glfwGetKey(window, GLFW_KEY_D) == 1) {
                double dx = Math.sin(Math.toRadians(cameraYaw + 90)) * speed;
                double dz = Math.cos(Math.toRadians(cameraYaw + 90)) * speed;
                cameraPos = new Point3D(cameraPos.getX() - dx, cameraPos.getY(), cameraPos.getZ() + dz);
            }
            try {
                double cursordx = currentCursorX.get() - lastCursorX.get();
                double cursordy = currentCursorY.get() - lastCursorY.get();
                cameraPitch += cursordy;
                cameraYaw += cursordx;
                lastCursorX = BufferUtils.createDoubleBuffer(1);
                lastCursorX.put(currentCursorX.get());
                lastCursorX.flip();
                lastCursorY = BufferUtils.createDoubleBuffer(1);
                lastCursorY.put(currentCursorY.get());
                lastCursorY.flip();
                currentCursorX = BufferUtils.createDoubleBuffer(1);
                currentCursorY = BufferUtils.createDoubleBuffer(1);
            } catch (BufferUnderflowException e) { }
        }
    }

    public static void main(String[] args) {
        new MainWindow().run();
    }

    void perspectiveGL( double fovY, double aspect, double zNear, double zFar )
    {
        double fW, fH;

        //fH = tan( (fovY / 2) / 180 * pi ) * zNear;
        fH = Math.tan(Math.toRadians(fovY)) * zNear;
        fW = fH * aspect;

        glFrustum( -fW, fW, -fH, fH, zNear, zFar );
    }

    void drawSector(Sector s) {
        for (Linedef l : s.linedefs) {

            if (renderList.contains(l)) {
                continue;
            } else {
                renderList.add(l);
            }

            if (alternate) {
                glColor3f(1f, 1f, 1f);
            } else {
                glColor3f(0.8f, 0.8f, 0.8f);
            }
            alternate = !alternate;

            if (l.mark) {
                glColor3f(1f, 0f, 0f);
            }
            try {
                glBegin(GL_QUADS);
                glVertex3f(-l.startVertex.x, l.leftSidedef.sector.floorHeight, l.startVertex.y);
                glVertex3f(-l.endVertex.x, l.leftSidedef.sector.floorHeight, l.endVertex.y);
                glVertex3f(-l.endVertex.x, l.rightSidedef.sector.floorHeight, l.endVertex.y);
                glVertex3f(-l.startVertex.x, l.rightSidedef.sector.floorHeight, l.startVertex.y);
                glEnd();
                glBegin(GL_QUADS);
                glVertex3f(-l.startVertex.x, l.leftSidedef.sector.ceilingHeight, l.startVertex.y);
                glVertex3f(-l.endVertex.x, l.leftSidedef.sector.ceilingHeight, l.endVertex.y);
                glVertex3f(-l.endVertex.x, l.rightSidedef.sector.ceilingHeight, l.endVertex.y);
                glVertex3f(-l.startVertex.x, l.rightSidedef.sector.ceilingHeight, l.startVertex.y);
                glEnd();


            } catch (NullPointerException e) {
                glBegin(GL_QUADS);
                glVertex3f(-l.startVertex.x, s.floorHeight, l.startVertex.y);
                glVertex3f(-l.startVertex.x, s.ceilingHeight, l.startVertex.y);
                glVertex3f(-l.endVertex.x, s.ceilingHeight, l.endVertex.y);
                glVertex3f(-l.endVertex.x, s.floorHeight, l.endVertex.y);
                glEnd();
            }
            if (l.mark) {
                if (alternate) {
                    glColor3f(1f, 1f, 1f);
                } else {
                    glColor3f(0.8f, 0.8f, 0.8f);
                }
            }
        }
        if (drawFloors) {
            if (alternate) {
                glColor3f(0.5f, 0.5f, 0.5f);
            } else {
                glColor3f(0.25f, 0.25f, 0.25f);
            }
            glBegin(GL_TRIANGLE_FAN);
            for (Linedef l : s.linedefs) {
                glVertex3f(-l.startVertex.x, s.floorHeight, l.startVertex.y);
                glVertex3f(-l.endVertex.x, s.floorHeight, l.endVertex.y);
            }
            glEnd();
            if (alternate) {
                glColor3f(0.5f, 0.5f, 0.5f);
            } else {
                glColor3f(0.25f, 0.25f, 0.25f);
            }
            /*glBegin(GL_TRIANGLE_FAN);
            for (Linedef l : s.linedefs) {
                glVertex3f(-l.startVertex.x, s.ceilingHeight, l.startVertex.y);
                glVertex3f(-l.endVertex.x, s.ceilingHeight, l.endVertex.y);
            }
            glEnd();*/


        }
        glColor3f(1f, 0f, 0f);
        glLineWidth(2.5f);
        glBegin(GL_LINES);
        for (int i = 0; i < s.linedefs.size(); i++) {
            Linedef l = s.linedefs.get(i);
            if (i != 0) {
                glVertex3f(-l.startVertex.x, s.floorHeight + 1, l.startVertex.y);
            }
            glVertex3f(-l.startVertex.x, s.floorHeight + 1, l.startVertex.y);
            glVertex3f(-l.endVertex.x, s.floorHeight + 1, l.endVertex.y);
            glVertex3f(-l.endVertex.x, s.floorHeight + 1, l.endVertex.y);
        }
        glEnd();

    }
    void drawLinedef(Linedef l) {
        glColor3f(1f, 1f, 1f);
        glVertex3f(-l.startVertex.x, -32, l.startVertex.y);
        glVertex3f(-l.startVertex.x, 32, l.startVertex.y);
        glVertex3f(-l.endVertex.x, 32, l.endVertex.y);
        glVertex3f(-l.endVertex.x, -32, l.endVertex.y);
    }
}

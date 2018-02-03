import javafx.geometry.Point3D;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class MainWindow {
    private long window;
    private int cameraPitch;
    private int cameraYaw;
    private Point3D cameraPos;

    public void run() {
        cameraPos = new Point3D(0, 0, 0);
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

    private void loop() {
        GL.createCapabilities();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glViewport(0, 0, 640, 480);
        perspectiveGL(30, 1.33333f, 0.1, 500);
        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_DEPTH_TEST);
        int i = 0;
        while (!glfwWindowShouldClose(window)) {
            int z = -200;
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glPushMatrix();

            glRotatef(cameraPitch, 1, 0, 0);
            glRotatef(cameraYaw, 0, 1, 0);
            glTranslatef((float) cameraPos.getX(), (float) cameraPos.getY(), (float) cameraPos.getZ());
            glTranslatef(0, 0, z);
            glRotatef(i, 0f, 1f, 0f);

            //glfwSetWindowTitle(window, Integer.toString(cameraYaw));


            glBegin(GL_QUADS);
            glColor3f(1, 1, 1);
            glVertex3f(-32, -32, -32);
            glVertex3f(32, -32, -32);
            glVertex3f(32, 32, -32);
            glVertex3f(-32, 32, -32);

            glVertex3f(-32, -32, 32);
            glVertex3f(32, -32, 32);
            glVertex3f(32, 32, 32);
            glVertex3f(-32, 32, 32);

            glColor3f(0.5f, 0.5f, 0.5f);

            glVertex3f(-32, -32, -32);
            glVertex3f(-32, -32, 32);
            glVertex3f(-32, 32, 32);
            glVertex3f(-32, 32, -32);

            glVertex3f(32, -32, -32);
            glVertex3f(32, -32, 32);
            glVertex3f(32, 32, 32);
            glVertex3f(32, 32, -32);

            glEnd();

            glPopMatrix();

            i = i >= 360 ? 0 : i + 1;

            glfwSwapBuffers(window);

            glfwPollEvents();

            int sensitivity = 1;
            int speed = 5;

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
            if ( glfwGetKey(window, GLFW_KEY_W) == 1) {
                double dx = Math.sin(Math.toRadians(cameraYaw)) * speed;
                double dz = Math.cos(Math.toRadians(cameraYaw)) * speed;
                System.out.println(dx + " " + dz);
                cameraPos = new Point3D(cameraPos.getX() + dx, 0, cameraPos.getZ() + dz);
                //cameraPos = new Point3D(cameraPos.getX(), cameraPos.getY(), cameraPos.getZ() + speed);
            }
            if ( glfwGetKey(window, GLFW_KEY_S) == 1) {
                double dx = Math.sin(Math.toRadians(cameraYaw)) * speed;
                double dz = Math.cos(Math.toRadians(cameraYaw)) * speed;
                cameraPos = new Point3D(cameraPos.getX() - dx, 0, cameraPos.getZ() - dz);
                //cameraPos = new Point3D(cameraPos.getX(), cameraPos.getY(), cameraPos.getZ() - speed);
            }




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
}

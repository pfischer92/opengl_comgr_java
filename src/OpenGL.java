import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class OpenGL
{
	public static void main(String[] args) throws Exception
	{
		// open a window
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 0);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
		long hWindow = GLFW.glfwCreateWindow(720, 480, "ComGr", 0, 0);
		GLFW.glfwSetWindowSizeCallback(hWindow, (window, width, height) -> {
			glViewport(0, 0, width, height);
		});
		GLFW.glfwMakeContextCurrent(hWindow);
		GLFW.glfwSwapInterval(1);
		createCapabilities();

		// set up opengl
		glEnable(GL_FRAMEBUFFER_SRGB);
		glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
		// glClearDepth(1);
		// glDisable(GL_DEPTH_TEST);
		// glDepthFunc(GL_LESS);
		// glDisable(GL_CULL_FACE);

		// load, compile and link shaders
		// see https://www.khronos.org/opengl/wiki/Vertex_Shader
		String VertexShaderSource =
						  "#version 400 core\n"
						+ "\n"
						+ "uniform float time;\n"
						+ "out float someFloat;\n"
						+ "in vec3 pos;\n"
						+ "\n"
						+ "void main()\n"
						+ "{\n"
						+ "  gl_Position = vec4(pos, 1.0) + vec4(sin(time) * 0.5, cos(time) * 0.5, 0.0, 0.0);\n"
						+ "  someFloat = pos.x + 0.5;\n"
						+ "}";
		int hVertexShader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(hVertexShader, VertexShaderSource);
		glCompileShader(hVertexShader);
		if (glGetShaderi(hVertexShader, GL_COMPILE_STATUS) != GL_TRUE)
			throw new Exception(glGetShaderInfoLog(hVertexShader));

		// see https://www.khronos.org/opengl/wiki/Fragment_Shader
		String FragmentShaderSource =
						  "#version 400 core\n"
						+ "\n"
						+ "out vec4 colour;\n"
						+ "in float someFloat;\n"
						+ "\n"
						+ "void main()\n"
						+ "{\n"
						+ "  colour = vec4(someFloat, 0.75, 0.0, 1.0);\n"
						+ "}";
		int hFragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(hFragmentShader, FragmentShaderSource);
		glCompileShader(hFragmentShader);
		if (glGetShaderi(hFragmentShader, GL_COMPILE_STATUS) != GL_TRUE)
			throw new Exception(glGetShaderInfoLog(hFragmentShader));

		// link shaders to a program
		int hProgram = glCreateProgram();
		glAttachShader(hProgram, hFragmentShader);
		glAttachShader(hProgram, hVertexShader);
		glLinkProgram(hProgram);
		if (glGetProgrami(hProgram, GL_LINK_STATUS) != GL_TRUE)
			throw new Exception(glGetProgramInfoLog(hProgram));

		// upload model vertices to a vbo
		float[] triangleVertices = new float[]
			{
				0.0f, -0.5f, 0.0f,
				0.5f, 0.5f, 0.0f,
				-0.5f, 0.5f, 0.0f
			};
		int vboTriangleVertices = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboTriangleVertices);
		glBufferData(GL_ARRAY_BUFFER, triangleVertices, GL_STATIC_DRAW);

		// upload model indices to a vbo
		int[] triangleIndices = new int[] { 0, 1, 2 };
		int vboTriangleIndices = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, triangleIndices, GL_STATIC_DRAW);
		
		// set up a vao
		int vaoTriangle = glGenVertexArrays();
		glBindVertexArray(vaoTriangle);
		int posAttribIndex = glGetAttribLocation(hProgram, "pos");
		if(posAttribIndex != -1)
		{
			glEnableVertexAttribArray(posAttribIndex);
			glBindBuffer(GL_ARRAY_BUFFER, vboTriangleVertices);
			glVertexAttribPointer(posAttribIndex, 3, GL_FLOAT, false, 0, 0);
		}
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);

		// check for errors during all previous calls
		int error = glGetError();
		if (error != GL_NO_ERROR)
			throw new Exception(Integer.toString(error));
		
		// render loop
		long startTime = System.currentTimeMillis();
		while (!GLFW.glfwWindowShouldClose(hWindow))
		{
			// clear screen and z-buffer
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			//model-view-projection matrix (not yet used)
			Mat4 mvp = Mat4.multiply(
					Mat4.perspective(45, 720f/480, 0.1f, 100f), //projection
					Mat4.lookAt(new Vec3(0, 0, -10), new Vec3(0, 0, 0), new Vec3(0, 1, 0)), //view
					Mat4.ID //model
				);
			
			// switch to our shader
			glUseProgram(hProgram);
			int timeUniformIndex = glGetUniformLocation(hProgram, "time");
			if(timeUniformIndex != -1)
				glUniform1f(timeUniformIndex, (float) (System.currentTimeMillis() - startTime) * 0.001f);
			
			// render our model
			glBindVertexArray(vaoTriangle);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);
			glDrawElements(GL_TRIANGLES, triangleIndices.length, GL_UNSIGNED_INT, 0);

			// display
			GLFW.glfwSwapBuffers(hWindow);
			GLFW.glfwPollEvents();

			error = glGetError();
			if (error != GL_NO_ERROR)
				throw new Exception(Integer.toString(error));
		}

		GLFW.glfwDestroyWindow(hWindow);
		GLFW.glfwTerminate();
	}
}

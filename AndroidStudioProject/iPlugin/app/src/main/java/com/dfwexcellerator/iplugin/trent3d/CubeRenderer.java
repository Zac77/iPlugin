package com.dfwexcellerator.iplugin.trent3d;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.dfwexcellerator.iplugin.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by quan on 2014/9/24.
 */
public class CubeRenderer implements GLSurfaceView.Renderer {

//    public float xAngle = 0;
//    public float yAngle = 0;

    // These still work without volatile, but refreshes are not guaranteed to happen.
    public volatile float mDeltaX;
    public volatile float mDeltaY;

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] mModelMatrix = new float[16];

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private float[] mViewMatrix = new float[16];

    /** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
    private float[] mProjectionMatrix = new float[16];

    /** Allocate storage for the final combined matrix. This will be passed into the shader program. */
    private float[] mMVPMatrix = new float[16];

    /** Store our model data in a float buffer. */
    private final FloatBuffer mCubeColors;

    private FloatBuffer mCubeVertices;
    private final FloatBuffer mCubeTextureCoordinates;

    /** This is used for indices matched in triangle1VerticesData*/
    short[] indices = {
        0, 1, 2, 2, 3, 0,
        4, 5, 7, 5, 6, 7,
        8, 9, 11, 9, 10, 11,
        12, 13, 15, 13, 14, 15,
        16, 17, 19, 17, 18, 19,
        20, 21, 23, 21, 22, 23,
    };

    ShortBuffer indexBuffer = null;

    /** This will be used to pass in the texture. */
    private int mTextureUniformHandle;

    /** This will be used to pass in model texture coordinate information. */
    private int mTextureCoordinateHandle;

    /** This will be used to pass in the transformation matrix. */
    private int mMVPMatrixHandle;

    /** This will be used to pass in model position information. */
    private int mPositionHandle;
    /** This is a handle to our cube shading program. */
    private int mProgramHandle;
    /** This will be used to pass in model color information. */
    private int mColorHandle;

    /** How many bytes per float. */
    private final int mBytesPerFloat = 4;

    /** Number of vertices on each face of cube*/
    private int numVertices = 4;

    /** Size of the position data in elements. */
    private final int mPositionDataSize = 3;

    /** Size of the color data in elements. */
    private final int mColorDataSize = 4;

    /** Size of the texture coordinate data in elements. */
    private final int mTextureCoordinateDataSize = 2;

    /** This is a handle to our texture data. */
    private int mTextureDataHandle0;
    private int mTextureDataHandle1;
    private int mTextureDataHandle2;
    private int mTextureDataHandle3;
    private int mTextureDataHandle4;
    private int mTextureDataHandle5;

    private final Context mActivityContext;

    private int mIndex;

    public CubeRenderer(final Context activityContext, int index){
        mActivityContext = activityContext;
        mIndex = index;
        // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle,
        // if the points are counter-clockwise we are looking at the "front". If not we are looking at
        // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
        // usually represent the backside of an object and aren't visible anyways.

        // Air condition cube position data
        final float[] cubePositionData1 = {
            // x, y, z
            // Front face
            -0.6f, -0.8f,  0.45f,    // v0
             0.6f, -0.8f,  0.45f,    // v1
             0.6f,  0.8f,  0.45f, 	 // v2
            -0.6f,  0.8f,  0.45f,    // v3

            // Right face
             0.6f, -0.8f,  0.45f,    // v4
             0.6f, -0.8f, -0.45f,    // v5
             0.6f,  0.8f, -0.45f,    // v6
             0.6f,  0.8f,  0.45f,    // v7

            // Back face
             0.6f, -0.8f, -0.45f,    // v8
            -0.6f, -0.8f, -0.45f,    // v9
            -0.6f,  0.8f, -0.45f,    // v10
             0.6f,  0.8f, -0.45f,    // v11

            // Left face
            -0.6f, -0.8f, -0.45f,     // v12
            -0.6f, -0.8f,  0.45f,     // v13
            -0.6f,  0.8f,  0.45f,     // v14
            -0.6f,  0.8f, -0.45f,     // v15

            // Top face
            -0.6f,  0.8f,  0.45f,     // v16
             0.6f,  0.8f,  0.45f,     // v17
             0.6f,  0.8f, -0.45f,     // v18
            -0.6f,  0.8f, -0.45f,     // v19

            // Bottom face
            -0.6f, -0.8f,  0.45f,     // v20
             0.6f, -0.8f,  0.45f,     // v21
             0.6f, -0.8f, -0.45f,     // v22
            -0.6f, -0.8f, -0.45f,     // v23
        };

        // Microwave cube position data
        final float[] cubePositionData2 = {
                // x, y, z
                // Front face
           -0.92f, -0.45f,  0.5f,	// v0
            0.92f, -0.45f,  0.5f, 	// v1
            0.92f,  0.45f,  0.5f, 	// v2
           -0.92f,  0.45f,  0.5f,   // v3

                // Right face
            0.92f, -0.45f,  0.5f,   // v4
            0.92f, -0.45f, -0.5f,   // v5
            0.92f,  0.45f, -0.5f,   // v6
            0.92f,  0.45f,  0.5f,   // v7

                // Back face
            0.92f, -0.45f, -0.5f,   // v8
           -0.92f, -0.45f, -0.5f,   // v9
           -0.92f,  0.45f, -0.5f,   // v10
            0.92f,  0.45f, -0.5f,   // v11

                // Left face
           -0.92f, -0.45f, -0.5f,    // v12
           -0.92f, -0.45f,  0.5f,    // v13
           -0.92f,  0.45f,  0.5f,    // v14
           -0.92f,  0.45f, -0.5f,    // v15

                // Top face
           -0.92f,  0.45f,  0.5f,    // v16
            0.92f,  0.45f,  0.5f,    // v17
            0.92f,  0.45f, -0.5f,    // v18
           -0.92f,  0.45f, -0.5f,    // v19

                // Bottom face
           -0.92f, -0.45f,  0.5f,    // v20
            0.92f, -0.45f,  0.5f,    // v21
            0.92f, -0.45f, -0.5f,    // v22
           -0.92f, -0.45f, -0.5f,    // v23
        };

        // Refrigerate cube position data
        final float[] cubePositionData3 = {
                // x, y, z
                // Front face
           -0.5f, -1.0f,  0.5f,	    // v0
            0.5f, -1.0f,  0.5f, 	// v1
            0.5f,  1.0f,  0.5f, 	// v2
           -0.5f,  1.0f,  0.5f,     // v3

                // Right face
            0.5f, -1.0f,  0.5f,     // v4
            0.5f, -1.0f, -0.5f,     // v5
            0.5f,  1.0f, -0.5f,     // v6
            0.5f,  1.0f,  0.5f,     // v7

                // Back face
            0.5f, -1.0f, -0.5f,     // v8
           -0.5f, -1.0f, -0.5f,     // v9
           -0.5f,  1.0f, -0.5f,     // v10
            0.5f,  1.0f, -0.5f,     // v11

                // Left face
           -0.5f, -1.0f, -0.5f,     // v12
           -0.5f, -1.0f,  0.5f,     // v13
           -0.5f,  1.0f,  0.5f,     // v14
           -0.5f,  1.0f, -0.5f,     // v15

                // Top face
           -0.5f,  1.0f,  0.5f,     // v16
            0.5f,  1.0f,  0.5f,     // v17
            0.5f,  1.0f, -0.5f,     // v18
           -0.5f,  1.0f, -0.5f,     // v19

                // Bottom face
           -0.5f, -1.0f,  0.5f,     // v20
            0.5f, -1.0f,  0.5f,     // v21
            0.5f, -1.0f, -0.5f,     // v22
           -0.5f, -1.0f, -0.5f,     // v23
        };

        // For RGBA color map method, each row contains 4 numbers, indicate a vertex point color,
        // and each face contains 4 vertex points, also, fragment points color will gradually change
        // between two vertex points.
        final float[] cubeColorData = {
            // R, G, B, A
            // Front face (white)
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,

            // Right face (white)
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,

            // Back face (white)
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,

            // Left face (white)
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,

            // Top face (white)
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,

            // Bottom face (white)
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
        };

        // S, T (or X, Y)
        // Texture coordinate data.
        // Because images have a Y axis pointing downward (values increase as you move down the image) while
        // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
        // What's more is that the texture coordinates are the same for every face.
        final float[] cubeTextureCoordinateData = {
            // Change map coordinates indices to relocate original bitmaps angles,
            // such as rotate 90/180.. degree
            // Front face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,

            // Right face
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            //0.0f, 1.0f,

            // Back face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,

            // Left face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,

            // Top face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,

            // Bottom face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
        };

        // Initialize the buffers.
        if(mIndex == 1){
            mCubeVertices = ByteBuffer.allocateDirect(cubePositionData1.length * mBytesPerFloat)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mCubeVertices.put(cubePositionData1).position(0);
        }

        if(mIndex == 2){
            mCubeVertices = ByteBuffer.allocateDirect(cubePositionData2.length * mBytesPerFloat)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mCubeVertices.put(cubePositionData2).position(0);
        }

        if(mIndex == 3){
            mCubeVertices = ByteBuffer.allocateDirect(cubePositionData3.length * mBytesPerFloat)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mCubeVertices.put(cubePositionData3).position(0);
        }

        mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);

        mCubeColors = ByteBuffer.allocateDirect(cubeColorData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeColors.put(cubeColorData).position(0);

        indexBuffer = ByteBuffer.allocateDirect(indices.length * 2)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        indexBuffer.put(indices).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config){
        // Set the background clear color to black.
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Use culling to remove back faces.(But do not apply to this project, otherwise the 6th face
        // of cube will dismiss)
        //GLES20.glEnable(GLES20.GL_CULL_FACE);

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // The below glEnable() call is a holdover from OpenGL ES 1, and is not needed in OpenGL ES 2.
        // Enable texture mapping
        // GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        // Air condition machine
        if(mIndex == 1){
            // Position the eye in front of the origin.
            final float eyeX = 0.0f;
            final float eyeY = 0.0f;
            final float eyeZ = 1.85f;

            // We are looking toward the distance
            final float lookX = 0.0f;
            final float lookY = 0.0f;
            final float lookZ = -5.0f;

            // Set our up vector. This is where our head would be pointing were we holding the camera.
            final float upX = 0.0f;
            final float upY = 1.0f;
            final float upZ = 0.0f;

            // Set the view matrix. This matrix can be said to represent the camera position.
            // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
            // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
            Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
        }

       // Microwave
       if(mIndex == 2){
           // Position the eye in front of the origin.
           final float eyeX = 0.0f;
           final float eyeY = 0.0f;
           final float eyeZ = 1.75f;

           // We are looking toward the distance
           final float lookX = 0.0f;
           final float lookY = 0.0f;
           final float lookZ = -5.0f;

           // Set our up vector. This is where our head would be pointing were we holding the camera.
           final float upX = 0.0f;
           final float upY = 1.0f;
           final float upZ = 0.0f;

           // Set the view matrix. This matrix can be said to represent the camera position.
           // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
           // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
           Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
       }

        // Refrigerator
        if(mIndex == 3){
            // Position the eye in front of the origin.
            final float eyeX = 0.0f;
            final float eyeY = 0.0f;
            final float eyeZ = 2.0f;

            // We are looking toward the distance
            final float lookX = 0.0f;
            final float lookY = 0.0f;
            final float lookZ = -5.0f;

            // Set our up vector. This is where our head would be pointing were we holding the camera.
            final float upX = 0.0f;
            final float upY = 1.0f;
            final float upZ = 0.0f;

            // Set the view matrix. This matrix can be said to represent the camera position.
            // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
            // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
            Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
        }

        // Please note i am making view matrix as identity matrix intentionally here to avoid the
        // effects of view matrix. if you want explore the effect of view matrix you can uncomment
        // this line
        //Matrix.setIdentityM(mViewMatrix, 0);

        final String vertexShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.per_pixel_vertex_shader_tex);
        final String fragmentShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.per_pixel_fragment_shader_tex);

        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                new String[] {"a_Position",  "a_Normal", "a_TexCoordinate"});

        // The texture pixels (Width * Height) must be multiples of 2, such as (256 * 512 / 256 * 256...)
        // Load textures of air condition
        if(mIndex == 1){
            mTextureDataHandle0 = TextureHelper.loadTexture(mActivityContext, R.drawable.airconditioner_front);   // Front
            mTextureDataHandle1 = TextureHelper.loadTexture(mActivityContext, R.drawable.airconditioner_right);   // Right
            mTextureDataHandle2 = TextureHelper.loadTexture(mActivityContext, R.drawable.airconditioner_back);    // Back
            mTextureDataHandle3 = TextureHelper.loadTexture(mActivityContext, R.drawable.airconditioner_left);    // Left
            mTextureDataHandle4 = TextureHelper.loadTexture(mActivityContext, R.drawable.airconditioner_top);     // Top
            mTextureDataHandle5 = TextureHelper.loadTexture(mActivityContext, R.drawable.airconditioner_bottom);  // Bottom
        }

        // Load textures of microwave
        if(mIndex == 2){
            mTextureDataHandle0 = TextureHelper.loadTexture(mActivityContext, R.drawable.microwave_front);   // Front
            mTextureDataHandle1 = TextureHelper.loadTexture(mActivityContext, R.drawable.microwave_right);   // Right
            mTextureDataHandle2 = TextureHelper.loadTexture(mActivityContext, R.drawable.microwave_back);    // Back
            mTextureDataHandle3 = TextureHelper.loadTexture(mActivityContext, R.drawable.microwave_left);    // Left
            mTextureDataHandle4 = TextureHelper.loadTexture(mActivityContext, R.drawable.microwave_top);     // Top
            mTextureDataHandle5 = TextureHelper.loadTexture(mActivityContext, R.drawable.microwave_bottom);  // Bottom
        }

        // Load textures of refrigerator
        if(mIndex == 3){
            mTextureDataHandle0 = TextureHelper.loadTexture(mActivityContext, R.drawable.refrigerator_front);   // Front
            mTextureDataHandle1 = TextureHelper.loadTexture(mActivityContext, R.drawable.refrigerator_right);   // Right
            mTextureDataHandle2 = TextureHelper.loadTexture(mActivityContext, R.drawable.refrigerator_back);    // Back
            mTextureDataHandle3 = TextureHelper.loadTexture(mActivityContext, R.drawable.refrigerator_left);    // Left
            mTextureDataHandle4 = TextureHelper.loadTexture(mActivityContext, R.drawable.refrigerator_top);     // Top
            mTextureDataHandle5 = TextureHelper.loadTexture(mActivityContext, R.drawable.refrigerator_bottom);  // Bottom
        }

    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height){
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        // air condition
        if(mIndex == 1){
            final float ratio = (float) width / height;
            final float left = -ratio;
            final float right = ratio;
            final float bottom = -0.78f;
            final float top = 0.78f;
            final float near = 0.77f;
            final float far = 10.0f;
            Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
        }

        // Microwave
        if(mIndex == 2){
            final float ratio = (float) width / height;
            final float left = -ratio;
            final float right = ratio;
            final float bottom = -0.75f;
            final float top = 0.75f;
            final float near = 0.63f;
            final float far = 10.0f;
            Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
        }

        // Refrigerator
        if(mIndex == 3){
            final float ratio = (float) width / height;
            final float left = -ratio;
            final float right = ratio;
            final float bottom = -0.88f;
            final float top = 0.88f;
            final float near = 0.78f;
            final float far = 10.0f;
            Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
        }

        // Please note i am making projection matrix as identity matrix intentionally here to avoid the
        // effects of projection matrix. if you want you can uncomment this line
        //Matrix.setIdentityM(mProjectionMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 glUnused){
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgramHandle);

        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");

        // Do a complete rotation every 10 seconds.
	    //long time = SystemClock.uptimeMillis() % 10000L;
        //float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        // Draw the triangle facing straight on.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, mDeltaX, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, mDeltaY, 1.0f, 0.0f, 0.0f);
	    //Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 1.0f, 1.0f);

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle0);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        draw(mCubeVertices, 0);

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle1);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 1);

        draw(mCubeVertices, 1);

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle2);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 2);

        draw(mCubeVertices, 2);

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle3);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 3);

        draw(mCubeVertices, 3);

        //Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE4);

        //Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle4);

        //Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 4);

        draw(mCubeVertices, 4);

        //Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE5);

        //Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle5);

        //Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 5);

        draw(mCubeVertices, 5);
    }

    private void draw(final FloatBuffer aCubeBuffer, final int i) {
        // Pass in the position information. each vertex needs 3 values and each face of the
        // cube needs 4 vertices. so total 3(mPositionDataSize) * 4(numVertices) = 12.
        aCubeBuffer.position(mPositionDataSize * numVertices * i);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                0, aCubeBuffer);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the color information. every vertex colr is defined by 4 values and each cube
        // face has 4 vertices so 4(mColorDataSize) * 4(numVertices) = 16.
        mCubeColors.position(mColorDataSize * numVertices * i);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                0, mCubeColors);

        GLES20.glEnableVertexAttribArray(mColorHandle);

        // Pass in the texture coordinate information. every vertex needs 2 values to define texture
        // for each face of the cube we need 4 vertices . so 2(mTextureCoordinateDataSize) * 4(numVertices) = 8
        mCubeTextureCoordinates.position(mTextureCoordinateDataSize * numVertices * i);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinates);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        //*each face of the cube is drawn using 2 triangles. so 2 * 3 = 6 lines
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
    }
}

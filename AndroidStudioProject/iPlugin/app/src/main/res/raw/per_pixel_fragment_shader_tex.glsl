precision mediump float;        // Set the default precision to medium. We don't need as high of a
                        		// precision in the fragment shader.
varying vec4 v_Color;          	// This is the color from the vertex shader interpolated across the
  								// triangle per fragment.
uniform sampler2D u_Texture;    // The input texture.
varying vec2 v_TexCoordinate;   // Interpolated texture coordinate per fragment.

// The entry point for our fragment shader.
void main()
{
    // Pass the color directly through the pipeline.
    gl_FragColor = (v_Color * texture2D(u_Texture, v_TexCoordinate));
}
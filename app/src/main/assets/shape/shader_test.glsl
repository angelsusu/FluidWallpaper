attribute vec2 position;
varying vec4 vertexColor;

void main()
{
    gl_Position = vec4(position, 0.0, 1.0);
    vertexColor = vec4(0.5f, 0.0f, 0.0f, 1.0f);
}
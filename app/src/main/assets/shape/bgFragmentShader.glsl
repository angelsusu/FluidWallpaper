uniform sampler2D ourTexture;
varying vec2 toFragmentCoord;

void main()
{
    gl_FragColor = texture2D(ourTexture, toFragmentCoord);
}
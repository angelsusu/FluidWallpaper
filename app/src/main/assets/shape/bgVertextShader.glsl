precision highp float;

attribute vec2 aPosition;
attribute vec2 texCoord;
varying vec2 toFragmentCoord;

void main () {
    gl_Position = vec4(aPosition, 0.0, 1.0);
    toFragmentCoord = texCoord;
}
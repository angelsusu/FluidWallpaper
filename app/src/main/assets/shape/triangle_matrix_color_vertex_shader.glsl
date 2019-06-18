//定义一个matrix。相当于4x4的矩阵
uniform mat4 u_Matrix;

attribute vec4 a_Position;
attribute vec4 a_Color;

varying vec4 v_Color;

void main() {
    v_Color = a_Color;
    //与position相乘
    gl_Position = u_Matrix* a_Position;
}

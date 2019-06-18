precision mediump float;

//在片元着色器这里添加这个 sampler2D 表示我们要添加2D贴图
uniform sampler2D u_TextureUnit;
//定义一个u_ChangeColor,因为颜色的变量是RGB,所以使用vec3
uniform int u_ChangeType;
uniform vec3 u_ChangeColor;
//定义一个屏幕宽高比
//uniform float uXY;

varying vec2 v_TextureCoordinates;

//定义一个放大镜的位置
//varying vec4 v_mag_Position;


//modifyColor.将color限制在rgb
void modifyColor(vec4 color){
    color.r=max(min(color.r,1.0),0.0);
    color.g=max(min(color.g,1.0),0.0);
    color.b=max(min(color.b,1.0),0.0);
    color.a=max(min(color.a,1.0),0.0);
}

void main(){
    //得到2d color
    vec4 nColor=texture2D(u_TextureUnit,v_TextureCoordinates);
    if(u_ChangeType==1){
        //黑白图片
        float c= nColor.r*u_ChangeColor.r+nColor.g*u_ChangeColor.g+nColor.b*u_ChangeColor.b;
        gl_FragColor = vec4(c,c,c,nColor.a);
    }else if(u_ChangeType==2){  //简单色彩处理，冷暖色调、增加亮度、降低亮度等
        vec4 deltaColor=nColor+vec4(u_ChangeColor,0.0);
        modifyColor(deltaColor);
        gl_FragColor=deltaColor;
    }else if(u_ChangeType==3){  //blur
        nColor+=texture2D(u_TextureUnit,vec2(v_TextureCoordinates.x-u_ChangeColor.r,v_TextureCoordinates.y-u_ChangeColor.r));
        nColor+=texture2D(u_TextureUnit,vec2(v_TextureCoordinates.x-u_ChangeColor.r,v_TextureCoordinates.y+u_ChangeColor.r));
        nColor+=texture2D(u_TextureUnit,vec2(v_TextureCoordinates.x+u_ChangeColor.r,v_TextureCoordinates.y-u_ChangeColor.r));
        nColor+=texture2D(u_TextureUnit,vec2(v_TextureCoordinates.x+u_ChangeColor.r,v_TextureCoordinates.y+u_ChangeColor.r));
        nColor+=texture2D(u_TextureUnit,vec2(v_TextureCoordinates.x-u_ChangeColor.g,v_TextureCoordinates.y-u_ChangeColor.g));
        nColor+=texture2D(u_TextureUnit,vec2(v_TextureCoordinates.x-u_ChangeColor.g,v_TextureCoordinates.y+u_ChangeColor.g));
        nColor+=texture2D(u_TextureUnit,vec2(v_TextureCoordinates.x+u_ChangeColor.g,v_TextureCoordinates.y-u_ChangeColor.g));
        nColor+=texture2D(u_TextureUnit,vec2(v_TextureCoordinates.x+u_ChangeColor.g,v_TextureCoordinates.y+u_ChangeColor.g));
        nColor+=texture2D(u_TextureUnit,vec2(v_TextureCoordinates.x-u_ChangeColor.b,v_TextureCoordinates.y-u_ChangeColor.b));
        nColor+=texture2D(u_TextureUnit,vec2(v_TextureCoordinates.x-u_ChangeColor.b,v_TextureCoordinates.y+u_ChangeColor.b));
        nColor+=texture2D(u_TextureUnit,vec2(v_TextureCoordinates.x+u_ChangeColor.b,v_TextureCoordinates.y-u_ChangeColor.b));
        nColor+=texture2D(u_TextureUnit,vec2(v_TextureCoordinates.x+u_ChangeColor.b,v_TextureCoordinates.y+u_ChangeColor.b));
        nColor/=13.0;
        gl_FragColor=nColor;
    }

    /*
    else if(u_ChangeType==4){  //放大镜效果

        float dis=distance(vec2(v_mag_Position.x,v_mag_Position.y/uXY),vec2(u_ChangeColor.r,u_ChangeColor.g));
        if(dis<u_ChangeColor.b){
            nColor=texture2D(u_TextureUnit,vec2(v_TextureCoordinates.x/2.0+0.25,v_TextureCoordinates.y/2.0+0.25));
        }
        gl_FragColor=nColor;

    }
    */

    else{
        gl_FragColor=nColor;
    }


}
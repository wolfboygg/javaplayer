attribute vec4 v_Position;
attribute vec2 f_Position;
varying vec2 ft_Position;
uniform float Time;

const float PI = 3.1415926;

void main() {


    float duration = 0.6;
    float maxAmplitude = 0.3;

    float time = mod(Time, duration);

    float amplitude = 1.0 + maxAmplitude * abs(sin(time * (PI / duration )));
     //顶点 放大
    gl_Position = vec4(v_Position.x * amplitude, v_Position.y * amplitude, v_Position.zw);
    ft_Position = f_Position;
}
